package com.secondhand.user.controller;

import com.secondhand.common.ApiResponse;
import com.secondhand.dto.request.UpdateSellerRequest;
import com.secondhand.entity.Seller;
import com.secondhand.feign.GoodsServiceClient;
import com.secondhand.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SellerController {

    private final SellerService service;
    private final GoodsServiceClient goodsServiceClient;

    @GetMapping("/seller/info")
    public ApiResponse<Seller> getInfo(@RequestAttribute("userId") Long userId) {
        return ApiResponse.success(service.getInfo(userId));
    }

    @PutMapping("/seller/info")
    public ApiResponse<Seller> updateInfo(@RequestAttribute("userId") Long userId,
                                          @RequestBody UpdateSellerRequest request) {
        return ApiResponse.success(service.updateInfo(userId, request));
    }

    @GetMapping("/seller/first-images")
    public ApiResponse<Map<Long, String>> firstImages(@RequestParam(value = "sellerIds", required = false) List<Long> sellerIds,
                                                     @RequestParam(value = "userIds", required = false) List<Long> userIds) {
        List<Long> ids = sellerIds != null ? sellerIds : userIds;
        return goodsServiceClient.getFirstImages(ids);
    }
}