package com.secondhand.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private List<Long> cartIds;

    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;

    private String remark;
}