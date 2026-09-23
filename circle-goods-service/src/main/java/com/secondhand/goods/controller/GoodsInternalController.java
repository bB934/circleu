package com.secondhand.goods.controller;

import com.secondhand.common.ApiResponse;
import com.secondhand.entity.Goods;
import com.secondhand.mapper.GoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/goods")
@RequiredArgsConstructor
public class GoodsInternalController {

    private final GoodsMapper goodsMapper;

    @GetMapping("/{goodsId}")
    public ApiResponse<Goods> getGoodsById(@PathVariable Long goodsId) {
        Goods goods = goodsMapper.findById(goodsId);
        return ApiResponse.success(goods);
    }

    @GetMapping("/{goodsId}/sellerId")
    public ApiResponse<Long> getSellerId(@PathVariable Long goodsId) {
        Goods goods = goodsMapper.findById(goodsId);
        return ApiResponse.success(goods != null ? goods.getSellerId() : null);
    }
}