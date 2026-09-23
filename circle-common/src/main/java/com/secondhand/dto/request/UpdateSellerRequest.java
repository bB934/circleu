package com.secondhand.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class UpdateSellerRequest {
    private String sellerGender;
    private Integer sellerAge;
    private String sellerSchool;
    private String sellerAddress;
    private LocalDate sellerBirthday;
    private String briefIntroduction;
}