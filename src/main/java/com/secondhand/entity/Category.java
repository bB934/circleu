package com.secondhand.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Category {
    private Long categoryId;
    private Long parentId;
    private String categoryName;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}