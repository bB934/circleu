package com.secondhand;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@MapperScan("com.secondhand.mapper")
public class SecondHandApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(SecondHandApplication.class)
                .profiles("local")
                .run(args);
    }
}