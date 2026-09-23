package com.secondhand.service;

import com.secondhand.common.PageResult;
import com.secondhand.dto.request.ConfirmReceiptRequest;
import com.secondhand.dto.request.CreateOrderRequest;
import com.secondhand.dto.request.RateOrderRequest;
import com.secondhand.dto.response.OrderResponse;
import com.secondhand.entity.Order;

public interface OrderService {
    Order createOrder(Long userId, CreateOrderRequest request);
    PageResult<OrderResponse> getOrderList(Long userId, String status, String keyword, int page, int size);
    OrderResponse getOrderDetail(Long userId, Long orderId);
    void payOrder(Long userId, Long orderId);
    void cancelOrder(Long userId, Long orderId);
    void confirmReceipt(Long userId, Long orderId);
    void rateOrder(Long userId, Long orderId, RateOrderRequest request);
    void deleteOrder(Long userId, Long orderId);

    // 卖家侧
    PageResult<OrderResponse> getSellerOrderList(Long merchantId, String status, String keyword, int page, int size);
    void shipOrder(Long merchantId, Long orderId);
}
