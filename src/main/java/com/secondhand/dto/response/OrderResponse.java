package com.secondhand.dto.response;

import com.secondhand.entity.Order;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderResponse {
    private Long orderId;
    private String orderNumber;
    private Long goodsId;
    private String title;
    private String img;
    private BigDecimal price;
    private BigDecimal priceAgo;
    private Integer num;
    private BigDecimal priceCount;
    private String description;
    private String contactName;
    private String contactPhone;
    private String contactAddress;
    private String postalCode;
    private String type;
    private String status;
    private Integer starRating;
    private String remarks;
    private String createTime;

    public static OrderResponse fromEntity(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderNumber(order.getOrderNumber());
        response.setGoodsId(order.getGoodsId());
        response.setTitle(order.getTitle());
        response.setImg(order.getImg());
        response.setPrice(order.getPrice());
        response.setPriceAgo(order.getPriceAgo());
        response.setNum(order.getNum());
        response.setPriceCount(order.getPriceCount());
        response.setDescription(order.getDescription());
        response.setContactName(order.getContactName());
        response.setContactPhone(order.getContactPhone());
        response.setContactAddress(order.getContactAddress());
        response.setPostalCode(order.getPostalCode());
        response.setType(order.getType());
        response.setStatus(order.getStatus());
        response.setStarRating(order.getStarRating());
        response.setRemarks(order.getRemarks());
        response.setCreateTime(order.getCreateTime() != null ? order.getCreateTime().toString() : null);
        return response;
    }
}