package com.secondhand.dto.response;

import com.secondhand.entity.Goods;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;  // ✅ 导入 LocalDateTime

@Data
public class GoodsResponse {
    private Long secondHandMallId;
    private Long sellerId;
    private Long categoryId;
    private String title;
    private String description;
    private String content;
    private BigDecimal price;
    private BigDecimal priceAgo;
    private Integer inventory;
    private Integer hits;
    private Integer status;
    private String coverImg;
    private String img1;
    private String img2;
    private String img3;
    private String img4;
    private String img5;
    private String sellerName;
    private String categoryName;
    private Boolean isFavorited;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static GoodsResponse fromEntity(Goods goods) {
        GoodsResponse response = new GoodsResponse();
        response.setSecondHandMallId(goods.getSecondHandMallId());
        response.setSellerId(goods.getSellerId());
        response.setCategoryId(goods.getCategoryId());
        response.setTitle(goods.getTitle());
        response.setDescription(goods.getDescription());
        response.setContent(goods.getContent());
        response.setPrice(goods.getPrice());
        response.setPriceAgo(goods.getPriceAgo());
        response.setInventory(goods.getInventory());
        response.setHits(goods.getHits());
        response.setStatus(goods.getStatus());
        response.setCoverImg(goods.getCoverImg());
        response.setImg1(goods.getImg1());
        response.setImg2(goods.getImg2());
        response.setImg3(goods.getImg3());
        response.setImg4(goods.getImg4());
        response.setImg5(goods.getImg5());
        response.setSellerName(goods.getSellerName());
        response.setCategoryName(goods.getCategoryName());
        response.setIsFavorited(goods.getIsFavorited());

        response.setCreateTime(goods.getCreateTime());
        response.setUpdateTime(goods.getUpdateTime());

        return response;
    }
}