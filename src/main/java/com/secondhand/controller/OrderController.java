package com.secondhand.controller;

import com.secondhand.common.ApiResponse;
import com.secondhand.common.PageResult;
import com.secondhand.annotation.OperationLog;
import com.secondhand.dto.request.CreateOrderRequest;
import com.secondhand.dto.request.RateOrderRequest;
import com.secondhand.dto.response.OrderResponse;
import com.secondhand.entity.Order;
import com.secondhand.service.OrderService;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    @OperationLog(value = "创建订单", description = "用户下单")
    @SentinelResource(value = "orderResource:create", blockHandler = "createBlockHandler")
    public ApiResponse<Order> createOrder(@RequestAttribute("userId") Long userId,
                                          @Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.success(orderService.createOrder(userId, request));
    }

    public ApiResponse<Order> createBlockHandler(Long userId, CreateOrderRequest request, BlockException ex) {
        log.warn("下单接口被Sentinel限流: {}", ex.getMessage());
        return ApiResponse.error("下单请求过于频繁，请稍后重试");
    }

    @PostMapping
    public ApiResponse<Order> createOrderAlias(@RequestAttribute("userId") Long userId,
                                               @Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.success(orderService.createOrder(userId, request));
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<OrderResponse>> getOrderList(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(orderService.getOrderList(userId, status, keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrderDetail(@RequestAttribute("userId") Long userId,
                                                     @PathVariable("id") Long orderId) {
        return ApiResponse.success(orderService.getOrderDetail(userId, orderId));
    }

    @PostMapping("/{id}/pay")
    @OperationLog(value = "支付订单", description = "用户支付订单")
    @SentinelResource(value = "orderResource:pay", blockHandler = "payBlockHandler",
            fallback = "payFallback")
    public ApiResponse<Map<String, String>> payOrder(@RequestAttribute("userId") Long userId,
                                                      @PathVariable("id") Long orderId) {
        orderService.payOrder(userId, orderId);
        // 模拟支付链接；接入真实支付时替换
        return ApiResponse.success(Map.of("payUrl", "https://pay.example.com/" + orderId));
    }

    public ApiResponse<Map<String, String>> payBlockHandler(Long userId, Long orderId, BlockException ex) {
        log.warn("支付接口被Sentinel熔断/限流: orderId={}", orderId);
        return ApiResponse.error("支付服务繁忙，请稍后重试");
    }

    public ApiResponse<Map<String, String>> payFallback(Long userId, Long orderId, Throwable t) {
        log.warn("支付接口降级: orderId={}, err={}", orderId, t.getMessage());
        return ApiResponse.error("支付暂时不可用，请稍后重试");
    }

    @PutMapping("/{id}/cancel")
    @OperationLog(value = "取消订单", description = "用户取消待付款订单")
    public ApiResponse<Void> cancelOrder(@RequestAttribute("userId") Long userId,
                                         @PathVariable("id") Long orderId) {
        orderService.cancelOrder(userId, orderId);
        return ApiResponse.success("订单已取消", null);
    }

    @PutMapping("/{id}/confirm")
    @OperationLog(value = "确认收货", description = "买家确认收货")
    public ApiResponse<Void> confirmReceipt(@RequestAttribute("userId") Long userId,
                                            @PathVariable("id") Long orderId) {
        orderService.confirmReceipt(userId, orderId);
        return ApiResponse.success("确认收货成功", null);
    }

    @PutMapping("/{id}/rate")
    @OperationLog(value = "评价订单", description = "买家评价订单")
    public ApiResponse<Void> rateOrder(@RequestAttribute("userId") Long userId,
                                       @PathVariable("id") Long orderId,
                                       @RequestBody RateOrderRequest request) {
        orderService.rateOrder(userId, orderId, request);
        return ApiResponse.success("评价成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOrder(@RequestAttribute("userId") Long userId,
                                         @PathVariable("id") Long orderId) {
        orderService.deleteOrder(userId, orderId);
        return ApiResponse.success("订单删除成功", null);
    }
}
