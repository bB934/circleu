package com.secondhand.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Announcement {
    private Long announcementId;
    private String title;
    private String content;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}