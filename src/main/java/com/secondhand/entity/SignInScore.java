package com.secondhand.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SignInScore {
    private Long signInScoreId;
    private String orderNumber;
    private Long purchaseUser;
    private Long business;
    private String purchaseGoods;
    private BigDecimal commodityPrice;
    private String signInStatus;
    private Integer starRating;
    private String remarks;
    private Integer recommend;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}