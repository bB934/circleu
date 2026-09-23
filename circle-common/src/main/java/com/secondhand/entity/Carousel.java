package com.secondhand.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Carousel {
    private Long carouselId;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}