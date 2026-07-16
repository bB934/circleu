package com.secondhand.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private Long merchantId;
    private Long goodsId;
    private String title;
    private String img;
    private BigDecimal price;
    private BigDecimal priceAgo;
    private Integer num;
    private BigDecimal priceCount;
    private String norms;
    private String type;
    private String description;
    private String contactName;
    private String contactPhone;
    private String contactAddress;
    private String postalCode;
    private String status;
    private Integer starRating;
    private String remarks;
    private Integer rated;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}