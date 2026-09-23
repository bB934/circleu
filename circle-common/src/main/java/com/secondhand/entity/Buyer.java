package com.secondhand.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Buyer {
    private Long buyersId;
    private Long userId;
    private String buyerNumber;
    private String buyerGender;
    private String buyerAge;
    private String buyerSchool;
    private String buyerAddress;
    private LocalDate buyerBirthday;
    private String briefIntroduction;
    private String examineState;
    private Integer recommend;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}