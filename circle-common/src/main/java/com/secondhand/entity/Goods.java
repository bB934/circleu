package com.secondhand.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Goods {
    private Long secondHandMallId;
    private Long sellerId;
    private Long categoryId;
    private String title;
    private String description;
    private String content;
    private BigDecimal price;
    private BigDecimal priceAgo;
    private Integer inventory;
    private Integer hits;
    private Integer recommend;
    private Integer status;
    private String coverImg;
    private String img1;
    private String img2;
    private String img3;
    private String img4;
    private String img5;
    private String remarks;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String sellerName;
    private String categoryName;
    private Boolean isFavorited;
}