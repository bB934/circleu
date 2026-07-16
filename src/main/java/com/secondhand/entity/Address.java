package com.secondhand.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Address {
    private Long addressId;
    private Long userId;
    private String name;
    private String phone;
    private String postcode;
    private String address;
    private Boolean isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}