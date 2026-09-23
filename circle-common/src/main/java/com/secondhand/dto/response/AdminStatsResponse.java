package com.secondhand.dto.response;

import lombok.Data;

@Data
public class AdminStatsResponse {
    private Long totalUsers;
    private Long totalGoods;
    private Long totalOrders;
    private Long pendingGoods;
    private Long pendingOrders;
}