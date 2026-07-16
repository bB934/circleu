package com.secondhand.dto.response;

import com.secondhand.entity.Cart;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartResponse {
    private Long cartId;
    private Long goodsId;
    private String title;
    private String img;
    private String description;
    private String type;
    private BigDecimal price;
    private BigDecimal priceAgo;
    private Integer num;
    private BigDecimal priceCount;
    private Boolean selected;
    private Integer stock;

    public static CartResponse fromEntity(Cart cart) {
        CartResponse response = new CartResponse();
        response.setCartId(cart.getCartId());
        response.setGoodsId(cart.getGoodsId());
        response.setPrice(cart.getPrice());
        response.setPriceAgo(cart.getPriceAgo());
        response.setNum(cart.getNum());
        response.setSelected(cart.getSelected() != null ? cart.getSelected() : true);
        return response;
    }
}