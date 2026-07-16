package com.secondhand.mq;

import com.secondhand.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OrderDelayProducer {

    private final RabbitTemplate rabbitTemplate;

    public OrderDelayProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // 订单创建后发送延迟消息（默认按待付款超时处理）
    public void sendDelayMessage(Order order, long delayMillis) {
        sendDelayMessage(order, delayMillis, OrderDelayMessage.TYPE_PAY_TIMEOUT);
    }

    // 发送带类型的延迟消息：PAY_TIMEOUT=待付款超时，SHIP_TIMEOUT=待发货超时
    public void sendDelayMessage(Order order, long delayMillis, String type) {
        try {
            OrderDelayMessage message = new OrderDelayMessage(
                    order.getOrderId(), order.getUserId(), order.getOrderNumber(), type);

            MessagePostProcessor ttl = msg -> {
                msg.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                return msg;
            };

            rabbitTemplate.convertAndSend(
                    com.secondhand.config.DelayMQConfig.DELAY_QUEUE, message, ttl);
            log.info("已发送订单超时检查延迟消息: orderId={}, type={}, delay={}ms",
                    order.getOrderId(), type, delayMillis);
        } catch (AmqpException e) {
            // MQ 不可用时不影响下单主流程，订单超时由定时兜底（如有）处理
            log.warn("发送延迟消息失败（MQ 可能未启动）: orderId={}", order.getOrderId(), e);
        }
    }
}
