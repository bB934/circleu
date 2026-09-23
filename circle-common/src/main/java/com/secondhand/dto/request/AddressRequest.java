package com.secondhand.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank(message = "收货人姓名不能为空")
    private String name;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String postcode;

    @NotBlank(message = "详细地址不能为空")
    private String address;

    private Boolean isDefault;
}