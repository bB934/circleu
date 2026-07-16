package com.secondhand.controller;

import com.secondhand.common.ApiResponse;
import com.secondhand.common.BusinessException;
import com.secondhand.common.PageResult;
import com.secondhand.annotation.OperationLog;
import com.secondhand.dto.request.AuditRequest;
import com.secondhand.dto.response.AdminStatsResponse;
import com.secondhand.entity.Goods;
import com.secondhand.entity.Order;
import com.secondhand.entity.User;
import com.secondhand.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ApiResponse<AdminStatsResponse> getStats(@RequestAttribute("role") String role) {
        checkAdmin(role);
        return ApiResponse.success(adminService.getStats());
    }

    @GetMapping("/users")
    public ApiResponse<PageResult<User>> getUserList(
            @RequestAttribute("role") String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String userRole,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        checkAdmin(role);
        return ApiResponse.success(adminService.getUserList(keyword, userRole, page, size));
    }

    @PutMapping("/users/{id}/audit")
    @OperationLog(value = "审核用户", description = "管理员审核用户")
    public ApiResponse<Void> auditUser(@RequestAttribute("role") String role,
                                       @PathVariable("id") Long userId,
                                       @Valid @RequestBody AuditRequest request) {
        checkAdmin(role);
        adminService.auditUser(userId, request.getPassed());
        return ApiResponse.success("审核成功", null);
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@RequestAttribute("role") String role,
                                        @PathVariable("id") Long userId) {
        checkAdmin(role);
        adminService.deleteUser(userId);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/goods")
    public ApiResponse<PageResult<Goods>> getGoodsList(
            @RequestAttribute("role") String role,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        checkAdmin(role);
        return ApiResponse.success(adminService.getGoodsList(keyword, status, page, size));
    }

    @PutMapping("/goods/{id}/audit")
    @OperationLog(value = "审核商品", description = "管理员审核商品")
    public ApiResponse<Void> auditGoods(@RequestAttribute("role") String role,
                                        @PathVariable("id") Long goodsId,
                                        @Valid @RequestBody AuditRequest request) {
        checkAdmin(role);
        adminService.auditGoods(goodsId, request.getPassed());
        return ApiResponse.success("审核成功", null);
    }

    @DeleteMapping("/goods/{id}")
    public ApiResponse<Void> deleteGoods(@RequestAttribute("role") String role,
                                         @PathVariable("id") Long goodsId) {
        checkAdmin(role);
        adminService.deleteGoods(goodsId);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/orders")
    public ApiResponse<PageResult<Order>> getOrderList(
            @RequestAttribute("role") String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        checkAdmin(role);
        return ApiResponse.success(adminService.getOrderList(status, page, size));
    }

    @PutMapping("/orders/{id}/refund")
    public ApiResponse<Void> forceRefund(@RequestAttribute("role") String role,
                                         @PathVariable("id") Long orderId) {
        checkAdmin(role);
        adminService.forceRefund(orderId);
        return ApiResponse.success("退款成功", null);
    }

    private void checkAdmin(String role) {
        if (!"admin".equals(role)) {
            throw new BusinessException(403, "无权限访问");
        }
    }
}