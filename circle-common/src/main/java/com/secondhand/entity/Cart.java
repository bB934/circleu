package com.secondhand.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Cart {
    private Long cartId;
    private Long userId;
    private Long goodsId;
    private Integer num;
    private BigDecimal price;
    private BigDecimal priceAgo;
    private Integer state;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String title;
    private String img;
    private String description;
    private String type;
    private BigDecimal priceCount;
    private Boolean selected;
}