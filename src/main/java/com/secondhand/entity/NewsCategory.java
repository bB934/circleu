package com.secondhand.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NewsCategory {
    private Long categoryId;
    private String categoryName;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}