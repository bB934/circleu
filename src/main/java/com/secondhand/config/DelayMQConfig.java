package com.secondhand.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DelayMQConfig {

    public static final String DELAY_QUEUE = "order.delay.queue";
    public static final String DEAD_EXCHANGE = "order.dead.exchange";
    public static final String DEAD_QUEUE = "order.dead.queue";
    public static final String DEAD_ROUTING_KEY = "order.dead";

    // 延迟队列：消息在此停留至 TTL 过期，再路由到死信交换机
    @Bean
    public Queue delayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_EXCHANGE);
        args.put("x-dead-letter-routing-key", DEAD_ROUTING_KEY);
        return new Queue(DELAY_QUEUE, true, false, false, args);
    }

    // 死信交换机（实际消费）
    @Bean
    public DirectExchange deadExchange() {
        return new DirectExchange(DEAD_EXCHANGE, true, false);
    }

    @Bean
    public Queue deadQueue() {
        return new Queue(DEAD_QUEUE, true);
    }

    @Bean
    public Binding deadBinding() {
        return BindingBuilder.bind(deadQueue()).to(deadExchange()).with(DEAD_ROUTING_KEY);
    }

    // 使用 JSON 消息转换器替代默认的 JDK 原生序列化，避免反序列化类白名单安全校验失败
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
