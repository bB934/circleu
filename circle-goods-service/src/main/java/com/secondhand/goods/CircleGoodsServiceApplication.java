package com.secondhand.goods;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.secondhand.mapper")
@ComponentScan(basePackages = {"com.secondhand.goods", "com.secondhand.service", "com.secondhand.config", "com.secondhand.util", "com.secondhand.mapper", "com.secondhand.annotation", "com.secondhand.aspect"})
public class CircleGoodsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CircleGoodsServiceApplication.class, args);
    }
}