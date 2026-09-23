package com.secondhand.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long userId;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String gender;
    private String avatar;
    private String role;
    private String examineState;
    private Integer recommend;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}