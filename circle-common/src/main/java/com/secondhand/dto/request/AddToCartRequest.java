package com.secondhand.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartRequest {
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    @NotNull(message = "数量不能为空")
    private Integer num;
}