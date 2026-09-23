package com.secondhand.service;

import com.secondhand.common.PageResult;
import com.secondhand.dto.response.AdminStatsResponse;
import com.secondhand.entity.Goods;
import com.secondhand.entity.Order;
import com.secondhand.entity.User;

public interface AdminService {
    AdminStatsResponse getStats();
    PageResult<User> getUserList(String keyword, String role, int page, int size);
    void auditUser(Long userId, Boolean passed);
    void deleteUser(Long userId);
    PageResult<Goods> getGoodsList(String keyword, Integer status, int page, int size);
    void auditGoods(Long goodsId, Boolean passed);
    void deleteGoods(Long goodsId);
    PageResult<Order> getOrderList(String status, int page, int size);
    void forceRefund(Long orderId);
}