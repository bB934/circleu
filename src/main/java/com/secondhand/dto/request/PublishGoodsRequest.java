package com.secondhand.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PublishGoodsRequest {
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotBlank(message = "商品标题不能为空")
    private String title;

    private String description;

    private String content;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private BigDecimal priceAgo;

    private Integer inventory;

    private String coverImg;

    private List<String> images;
}