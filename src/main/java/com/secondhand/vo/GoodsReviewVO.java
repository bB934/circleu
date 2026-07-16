package com.secondhand.vo;

import lombok.Data;

@Data
public class GoodsReviewVO {
    private Long reviewId;
    private Long goodsId;
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer rating;
    private String content;
    private String createTime;
}
