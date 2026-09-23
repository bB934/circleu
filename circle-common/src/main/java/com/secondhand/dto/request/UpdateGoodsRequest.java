package com.secondhand.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateGoodsRequest {
    private Long categoryId;
    private String title;
    private String description;
    private String content;
    private BigDecimal price;
    private BigDecimal priceAgo;
    private Integer inventory;
    private String coverImg;
    private List<String> images;
}