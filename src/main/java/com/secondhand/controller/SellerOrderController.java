package com.secondhand.controller;

import com.secondhand.common.ApiResponse;
import com.secondhand.common.BusinessException;
import com.secondhand.common.PageResult;
import com.secondhand.dto.response.OrderResponse;
import com.secondhand.entity.Seller;
import com.secondhand.mapper.SellerMapper;
import com.secondhand.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SellerOrderController {

    private final OrderService orderService;
    private final SellerMapper sellerMapper;

    @GetMapping("/seller/orders")
    public ApiResponse<PageResult<OrderResponse>> list(@RequestAttribute("userId") Long userId,
                                                       @RequestParam(required = false) String status,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        Long merchantId = resolveSellerId(userId);
        return ApiResponse.success(orderService.getSellerOrderList(merchantId, status, keyword, page, size));
    }

    @PutMapping("/order/{id}/ship")
    public ApiResponse<Void> ship(@RequestAttribute("userId") Long userId,
                                  @PathVariable("id") Long id) {
        Long merchantId = resolveSellerId(userId);
        orderService.shipOrder(merchantId, id);
        return ApiResponse.success("发货成功", null);
    }

    private Long resolveSellerId(Long userId) {
        Seller seller = sellerMapper.findByUserId(userId);
        if (seller == null) {
            throw new BusinessException("当前用户不是卖家");
        }
        return seller.getSellerId();
    }
}
