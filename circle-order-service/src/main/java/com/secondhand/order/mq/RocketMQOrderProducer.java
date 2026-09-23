package com.secondhand.order.mq;

import com.secondhand.entity.Order;
import com.secondhand.mq.OrderDelayMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RocketMQOrderProducer {

    private static final String TOPIC = "order_delay_topic";

    private final RocketMQTemplate rocketMQTemplate;

    public RocketMQOrderProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public void sendDelayMessage(Order order, long delayMillis) {
        sendDelayMessage(order, delayMillis, OrderDelayMessage.TYPE_PAY_TIMEOUT);
    }

    public void sendDelayMessage(Order order, long delayMillis, String type) {
        try {
            OrderDelayMessage message = new OrderDelayMessage(
                    order.getOrderId(), order.getUserId(), order.getOrderNumber(), type);

            int delayLevel = toDelayLevel(delayMillis);
            if (delayLevel > 0) {
                rocketMQTemplate.syncSend(TOPIC,
                        MessageBuilder.withPayload(message).build(),
                        3000, delayLevel);
            } else {
                long delaySeconds = delayMillis / 1000;
                rocketMQTemplate.syncSendDelayTimeSeconds(TOPIC,
                        MessageBuilder.withPayload(message).build(), delaySeconds);
            }
            log.info("Sent order timeout delay message: orderId={}, type={}, delay={}ms",
                    order.getOrderId(), type, delayMillis);
        } catch (Exception e) {
            log.warn("Failed to send delay message (RocketMQ may not be started): orderId={}", order.getOrderId(), e);
        }
    }

    private int toDelayLevel(long delayMillis) {
        if (delayMillis <= 1_000) return 1;
        if (delayMillis <= 5_000) return 2;
        if (delayMillis <= 10_000) return 3;
        if (delayMillis <= 30_000) return 4;
        if (delayMillis <= 60_000) return 5;
        if (delayMillis <= 120_000) return 6;
        if (delayMillis <= 180_000) return 7;
        if (delayMillis <= 240_000) return 8;
        if (delayMillis <= 300_000) return 9;
        if (delayMillis <= 360_000) return 10;
        if (delayMillis <= 420_000) return 11;
        if (delayMillis <= 480_000) return 12;
        if (delayMillis <= 540_000) return 13;
        if (delayMillis <= 600_000) return 14;
        if (delayMillis <= 1_200_000) return 15;
        if (delayMillis <= 1_800_000) return 16;
        if (delayMillis <= 3_600_000) return 17;
        if (delayMillis <= 7_200_000) return 18;
        return -1;
    }
}