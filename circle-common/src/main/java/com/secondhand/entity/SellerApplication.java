package com.secondhand.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SellerApplication {
    private Long applicationId;
    private Long userId;
    private String realName;
    private String phone;
    private String gender;
    private Integer age;
    private String school;
    private String address;
    private LocalDate birthday;
    private String introduction;
    private String status;  // pending, approved, rejected
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}