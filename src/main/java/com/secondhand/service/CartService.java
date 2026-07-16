package com.secondhand.service;

import com.secondhand.dto.request.AddToCartRequest;
import com.secondhand.dto.request.UpdateCartRequest;
import com.secondhand.dto.response.CartResponse;
import java.util.List;

public interface CartService {
    List<CartResponse> getCartList(Long userId);
    void addToCart(Long userId, AddToCartRequest request);
    void updateCart(Long userId, Long cartId, UpdateCartRequest request);
    void deleteCartItem(Long userId, Long cartId);
    void selectAll(Long userId, boolean selected);
    void deleteSelected(Long userId);
}