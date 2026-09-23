package com.secondhand.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Seller {
    private Long sellerId;
    private Long userId;
    private String sellerNumber;
    private String sellerGender;
    private Integer sellerAge;
    private String sellerSchool;
    private String sellerAddress;
    private LocalDate sellerBirthday;
    private String briefIntroduction;
    private String examineState;
    private Integer creditScore;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}