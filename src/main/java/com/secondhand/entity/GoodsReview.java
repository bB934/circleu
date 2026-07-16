package com.secondhand.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GoodsReview {
    private Long reviewId;
    private Long orderId;
    private Long goodsId;
    private Long userId;
    private Integer rating;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
