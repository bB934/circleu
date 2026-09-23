package com.secondhand.service;

import com.secondhand.common.PageResult;
import com.secondhand.dto.request.ApplySellerRequest;
import com.secondhand.dto.request.AuditApplicationRequest;
import com.secondhand.entity.SellerApplication;

import java.util.Map;

public interface SellerApplicationService {
    void apply(Long userId, ApplySellerRequest request);
    Map<String, Object> getStatus(Long userId);
    void cancelApply(Long userId);
    PageResult<SellerApplication> getApplicationList(String status, String keyword, int page, int size);
    void audit(Long applicationId, AuditApplicationRequest request);
}