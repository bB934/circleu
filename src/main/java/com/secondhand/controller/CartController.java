package com.secondhand.controller;

import com.secondhand.common.ApiResponse;
import com.secondhand.dto.request.AddToCartRequest;
import com.secondhand.dto.request.UpdateCartRequest;
import com.secondhand.dto.response.CartResponse;
import com.secondhand.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/list")
    public ApiResponse<List<CartResponse>> getCartList(@RequestAttribute("userId") Long userId) {
        return ApiResponse.success(cartService.getCartList(userId));
    }

    @PostMapping
    public ApiResponse<Void> addToCart(@RequestAttribute("userId") Long userId,
                                       @Valid @RequestBody AddToCartRequest request) {
        cartService.addToCart(userId, request);
        return ApiResponse.success("添加成功", null);
    }

    @PutMapping("/{cartId}")
    public ApiResponse<Void> updateCart(@RequestAttribute("userId") Long userId,
                                        @PathVariable("cartId") Long cartId,
                                        @Valid @RequestBody UpdateCartRequest request) {
        cartService.updateCart(userId, cartId, request);
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/{cartId}")
    public ApiResponse<Void> deleteCartItem(@RequestAttribute("userId") Long userId,
                                            @PathVariable("cartId") Long cartId) {
        cartService.deleteCartItem(userId, cartId);
        return ApiResponse.success("删除成功", null);
    }

    @PutMapping("/select-all")
    public ApiResponse<Void> selectAll(@RequestAttribute("userId") Long userId,
                                       @RequestParam boolean selected) {
        cartService.selectAll(userId, selected);
        return ApiResponse.success("操作成功", null);
    }

    @DeleteMapping("/selected")
    public ApiResponse<Void> deleteSelected(@RequestAttribute("userId") Long userId) {
        cartService.deleteSelected(userId);
        return ApiResponse.success("删除成功", null);
    }
}