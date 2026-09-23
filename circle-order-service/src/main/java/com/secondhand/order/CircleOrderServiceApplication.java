package com.secondhand.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.secondhand.feign", value = "circle-order-service")
@MapperScan("com.secondhand.mapper")
@ComponentScan(basePackages = {"com.secondhand.order", "com.secondhand.service", "com.secondhand.config", "com.secondhand.util", "com.secondhand.mapper", "com.secondhand.annotation", "com.secondhand.aspect"})
public class CircleOrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CircleOrderServiceApplication.class, args);
    }
}