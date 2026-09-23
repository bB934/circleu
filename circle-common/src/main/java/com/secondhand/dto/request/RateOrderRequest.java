package com.secondhand.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class RateOrderRequest {
    @NotNull(message = "请评分")
    @Min(1)
    @Max(5)
    private Integer starRating;
    private String remarks;
}
