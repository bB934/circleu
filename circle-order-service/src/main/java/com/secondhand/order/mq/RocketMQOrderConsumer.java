package com.secondhand.order.mq;

import com.secondhand.entity.Goods;
import com.secondhand.entity.Order;
import com.secondhand.feign.StockFeignClient;
import com.secondhand.mapper.GoodsMapper;
import com.secondhand.mapper.OrderMapper;
import com.secondhand.mq.OrderDelayMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
//@RocketMQMessageListener(topic = "order_delay_topic", consumerGroup = "order_delay_consumer_group")
public class RocketMQOrderConsumer implements RocketMQListener<OrderDelayMessage> {

    private final OrderMapper orderMapper;
    private final GoodsMapper goodsMapper;
    private final StockFeignClient stockFeignClient;

    public RocketMQOrderConsumer(OrderMapper orderMapper, GoodsMapper goodsMapper, StockFeignClient stockFeignClient) {
        this.orderMapper = orderMapper;
        this.goodsMapper = goodsMapper;
        this.stockFeignClient = stockFeignClient;
    }

    @Override
    @Transactional
    public void onMessage(OrderDelayMessage message) {
        log.info("Received order timeout check message: orderId={}, type={}", message.getOrderId(), message.getType());

        Order order;
        try {
            order = orderMapper.findById(message.getOrderId());
        } catch (Exception e) {
            log.error("Failed to query order: orderId={}", message.getOrderId(), e);
            return;
        }
        if (order == null) {
            log.info("Order does not exist, ignoring: orderId={}", message.getOrderId());
            return;
        }

        String type = message.getType();
        if (OrderDelayMessage.TYPE_SHIP_TIMEOUT.equals(type)) {
            if (!"待发货".equals(order.getStatus())) {
                log.info("Order is no longer pending shipment: orderId={}, status={}",
                        message.getOrderId(), order.getStatus());
                return;
            }
            cancelAndRestore(order, "待发货超时（24h未发货）自动取消");
        } else {
            if (!"待付款".equals(order.getStatus())) {
                log.info("Order is no longer pending payment: orderId={}, status={}",
                        message.getOrderId(), order.getStatus());
                return;
            }
            cancelAndRestore(order, "待付款超时自动取消");
        }
    }

    private void cancelAndRestore(Order order, String reason) {
        order.setStatus("已取消");
        orderMapper.update(order);

        Goods goods = goodsMapper.findById(order.getGoodsId());
        if (goods != null) {
            goods.setInventory(goods.getInventory() + order.getNum());
            goodsMapper.update(goods);
            stockFeignClient.increaseStock(order.getGoodsId(), order.getNum());
        }

        // 超时取消回补库存后，重新计算可售库存：仍有剩余则重新上架，否则保持已售
        int inventory = (goods != null && goods.getInventory() != null) ? goods.getInventory() : 0;
        int occupied = orderMapper.sumPendingStock(order.getGoodsId());
        goodsMapper.updateStatus(order.getGoodsId(), (inventory - occupied) <= 0 ? 2 : 1);

        log.info("{}并回补库存: orderId={}, goodsId={}, num={}",
                reason, order.getOrderId(), order.getGoodsId(), order.getNum());
    }
}