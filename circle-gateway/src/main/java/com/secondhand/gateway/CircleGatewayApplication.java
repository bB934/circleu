package com.secondhand.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.secondhand.gateway", "com.secondhand.util"})
public class CircleGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(CircleGatewayApplication.class, args);
    }
}