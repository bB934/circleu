package com.secondhand.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Long commentId;
    private Long userId;
    private Long replyToId;
    private String sourceTable;
    private String sourceField;
    private Long sourceId;
    private String content;
    private String nickname;
    private String avatar;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}