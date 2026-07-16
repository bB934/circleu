package com.secondhand.dto.request;

import lombok.Data;

@Data
public class ConfirmReceiptRequest {
    private Integer starRating;
    private String remarks;
}