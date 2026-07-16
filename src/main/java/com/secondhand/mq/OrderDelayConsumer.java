package com.secondhand.mq;

import com.secondhand.common.BusinessException;
import com.secondhand.entity.Order;
import com.secondhand.mapper.OrderMapper;
import com.secondhand.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.secondhand.config.DelayMQConfig.DEAD_QUEUE;

@Slf4j
@Component
public class OrderDelayConsumer {

    private static final String IDEMPOTENT_PREFIX = "order:delay:handled:";

    private final OrderMapper orderMapper;
    private final StockService stockService;
    private final StringRedisTemplate redisTemplate;

    public OrderDelayConsumer(OrderMapper orderMapper, StockService stockService,
                              StringRedisTemplate redisTemplate) {
        this.orderMapper = orderMapper;
        this.stockService = stockService;
        this.redisTemplate = redisTemplate;
    }

    @RabbitListener(queues = DEAD_QUEUE)
    @Transactional
    public void handleTimeout(OrderDelayMessage message) {
        log.info("收到订单超时检查消息: orderId={}, type={}", message.getOrderId(), message.getType());

        Order order;
        try {
            order = orderMapper.findById(message.getOrderId());
        } catch (Exception e) {
            log.error("查询订单失败: orderId={}", message.getOrderId(), e);
            return;
        }
        if (order == null) {
            log.info("订单不存在，忽略: orderId={}", message.getOrderId());
            return;
        }

        // 幂等：基于 Redis 唯一流水号(订单ID+类型)SETNX，彻底避免网络重发重复处理
        String idemKey = IDEMPOTENT_PREFIX + message.getOrderId() + ":" + message.getType();
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(idemKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(acquired)) {
            log.info("延迟消息重复消费，已幂等跳过: orderId={}, type={}",
                    message.getOrderId(), message.getType());
            return;
        }

        // 幂等：根据消息类型校验当前订单状态是否仍需处理
        String type = message.getType();
        if (OrderDelayMessage.TYPE_SHIP_TIMEOUT.equals(type)) {
            // 待发货超时（24h）：卖家未发货 → 自动退货，回补库存并取消
            if (!"待发货".equals(order.getStatus())) {
                log.info("订单已非待发货状态，无需处理: orderId={}, status={}",
                        message.getOrderId(), order.getStatus());
                return;
            }
            cancelAndRestore(order, "待发货超时（24h未发货）自动取消");
        } else {
            // 默认/待付款超时（15min）：未支付 → 自动取消并回补库存
            if (!"待付款".equals(order.getStatus())) {
                log.info("订单已非待付款状态，无需取消: orderId={}, status={}",
                        message.getOrderId(), order.getStatus());
                return;
            }
            cancelAndRestore(order, "待付款超时自动取消");
        }
    }

    private void cancelAndRestore(Order order, String reason) {
        order.setStatus("已取消");
        orderMapper.update(order);

        stockService.restoreStock(order.getGoodsId(), order.getNum());
        log.info("{}并回补库存: orderId={}, goodsId={}, num={}",
                reason, order.getOrderId(), order.getGoodsId(), order.getNum());
    }
}
