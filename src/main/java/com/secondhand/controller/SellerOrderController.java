package com.secondhand.controller;

import com.secondhand.common.ApiResponse;
import com.secondhand.common.PageResult;
import com.secondhand.dto.response.OrderResponse;
import com.secondhand.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SellerOrderController {

    private final OrderService orderService;

    @GetMapping("/seller/orders")
    public ApiResponse<PageResult<OrderResponse>> list(@RequestAttribute("userId") Long merchantId,
                                                       @RequestParam(required = false) String status,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(orderService.getSellerOrderList(merchantId, status, keyword, page, size));
    }

    @PutMapping("/order/{id}/ship")
    public ApiResponse<Void> ship(@RequestAttribute("userId") Long merchantId,
                                  @PathVariable("id") Long id) {
        orderService.shipOrder(merchantId, id);
        return ApiResponse.success("发货成功", null);
    }
}
