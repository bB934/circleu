package com.secondhand.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class News {
    private Long newsId;
    private Long categoryId;
    private String title;
    private String summary;
    private String content;
    private String coverImg;
    private Integer hits;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}