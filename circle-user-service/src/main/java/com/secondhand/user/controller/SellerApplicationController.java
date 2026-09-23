package com.secondhand.user.controller;

import com.secondhand.common.ApiResponse;
import com.secondhand.common.BusinessException;
import com.secondhand.common.PageResult;
import com.secondhand.dto.request.ApplySellerRequest;
import com.secondhand.dto.request.AuditApplicationRequest;
import com.secondhand.entity.SellerApplication;
import com.secondhand.service.SellerApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SellerApplicationController {

    private final SellerApplicationService service;

    // 申请成为卖家
    @PostMapping("/user/apply-seller")
    public ApiResponse<Void> apply(@RequestAttribute("userId") Long userId,
                                   @Valid @RequestBody ApplySellerRequest request) {
        service.apply(userId, request);
        return ApiResponse.success("申请提交成功，请等待审核", null);
    }

    // 查询申请状态
    @GetMapping("/user/apply-status")
    public ApiResponse<Map<String, Object>> getStatus(@RequestAttribute("userId") Long userId) {
        return ApiResponse.success(service.getStatus(userId));
    }

    // 取消申请
    @DeleteMapping("/user/apply-seller")
    public ApiResponse<Void> cancelApply(@RequestAttribute("userId") Long userId) {
        service.cancelApply(userId);
        return ApiResponse.success("申请已取消", null);
    }

    // 管理员 - 获取申请列表
    @GetMapping("/admin/seller-applications")
    public ApiResponse<PageResult<SellerApplication>> getApplicationList(
            @RequestAttribute("role") String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        checkAdmin(role);
        return ApiResponse.success(service.getApplicationList(status, keyword, page, size));
    }

    // 管理员 - 审核申请
    @PutMapping("/admin/seller-applications/{id}/audit")
    public ApiResponse<Void> audit(@RequestAttribute("role") String role,
                                   @PathVariable("id") Long applicationId,
                                   @Valid @RequestBody AuditApplicationRequest request) {
        checkAdmin(role);
        service.audit(applicationId, request);
        return ApiResponse.success("审核成功", null);
    }

    private void checkAdmin(String role) {
        if (!"admin".equals(role)) {
            throw new BusinessException(403, "无权限访问");
        }
    }
}