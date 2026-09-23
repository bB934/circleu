package com.secondhand.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ForumPost {
    private Long postId;
    private Long userId;
    private String title;
    private String content;
    private String category;
    private Integer hits;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}