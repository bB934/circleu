package com.secondhand.mq;

import java.io.Serializable;

public class OrderDelayMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_PAY_TIMEOUT = "PAY_TIMEOUT";
    public static final String TYPE_SHIP_TIMEOUT = "SHIP_TIMEOUT";

    private Long orderId;
    private Long userId;
    private String orderNumber;
    private String type;

    public OrderDelayMessage() {}

    public OrderDelayMessage(Long orderId, Long userId, String orderNumber, String type) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderNumber = orderNumber;
        this.type = type;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}