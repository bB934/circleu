package com.secondhand.order.service.impl;

import com.secondhand.common.BusinessException;
import com.secondhand.dto.request.AddToCartRequest;
import com.secondhand.dto.request.UpdateCartRequest;
import com.secondhand.dto.response.CartResponse;
import com.secondhand.entity.Cart;
import com.secondhand.entity.Goods;
import com.secondhand.mapper.CartMapper;
import com.secondhand.mapper.GoodsMapper;
import com.secondhand.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final GoodsMapper goodsMapper;

    @Override
    public List<CartResponse> getCartList(Long userId) {
        List<Cart> cartList = cartMapper.findByUserId(userId);
        return cartList.stream()
                .map(cart -> {
                    CartResponse response = CartResponse.fromEntity(cart);
                    Goods goods = goodsMapper.findById(cart.getGoodsId());
                    if (goods != null) {
                        response.setTitle(goods.getTitle());
                        response.setImg(goods.getCoverImg());
                        response.setDescription(goods.getDescription());
                        response.setPriceCount(cart.getPrice().multiply(BigDecimal.valueOf(cart.getNum())));
                        response.setStock(goods.getInventory());
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addToCart(Long userId, AddToCartRequest request) {
        Goods goods = goodsMapper.findById(request.getGoodsId());
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        if (goods.getStatus() != 1) {
            throw new BusinessException("商品已下架或已售出");
        }
        if (goods.getInventory() < request.getNum()) {
            throw new BusinessException("库存不足");
        }

        Cart existing = cartMapper.findByUserIdAndGoodsId(userId, request.getGoodsId());
        if (existing != null && existing.getState() == 1) {
            int newNum = existing.getNum() + request.getNum();
            if (newNum > goods.getInventory()) {
                throw new BusinessException("库存不足");
            }
            existing.setNum(newNum);
            cartMapper.update(existing);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setGoodsId(request.getGoodsId());
            cart.setNum(request.getNum());
            cart.setPrice(goods.getPrice());
            cart.setPriceAgo(goods.getPriceAgo());
            cart.setState(1);
            cartMapper.insert(cart);
        }
    }

    @Override
    public void updateCart(Long userId, Long cartId, UpdateCartRequest request) {
        Cart cart = cartMapper.findById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车商品不存在");
        }
        if (request.getNum() <= 0) {
            cartMapper.deleteById(cartId);
            return;
        }
        Goods goods = goodsMapper.findById(cart.getGoodsId());
        if (goods != null && request.getNum() > goods.getInventory()) {
            throw new BusinessException("库存不足");
        }
        cart.setNum(request.getNum());
        cartMapper.update(cart);
    }

    @Override
    public void deleteCartItem(Long userId, Long cartId) {
        Cart cart = cartMapper.findById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车商品不存在");
        }
        cartMapper.deleteById(cartId);
    }

    @Override
    public void selectAll(Long userId, boolean selected) {
        // 全选/全不选是前端状态，不需要持久化
    }

    @Override
    @Transactional
    public void deleteSelected(Long userId) {
        cartMapper.deleteByUserId(userId);
    }
}