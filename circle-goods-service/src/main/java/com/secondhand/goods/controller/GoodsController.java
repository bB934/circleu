package com.secondhand.goods.controller;

import com.secondhand.common.ApiResponse;
import com.secondhand.common.PageResult;
import com.secondhand.dto.request.PublishGoodsRequest;
import com.secondhand.dto.request.UpdateGoodsRequest;
import com.secondhand.dto.response.GoodsResponse;
import com.secondhand.entity.Category;
import com.secondhand.service.GoodsService;
import com.secondhand.vo.GoodsReviewResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    @GetMapping("/list")
    public ApiResponse<PageResult<GoodsResponse>> getGoodsList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(goodsService.getGoodsList(keyword, categoryId, minPrice, maxPrice, sortBy, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<GoodsResponse> getGoodsDetail(@PathVariable("id") Long goodsId,
                                                     @RequestAttribute(value = "userId", required = false) Long userId) {
        return ApiResponse.success(goodsService.getGoodsDetail(goodsId, userId));
    }

    @PostMapping
    public ApiResponse<Void> publishGoods(@RequestAttribute("userId") Long userId,
                                          @Valid @RequestBody PublishGoodsRequest request) {
        goodsService.publishGoods(userId, request);
        return ApiResponse.success("商品发布成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateGoods(@RequestAttribute("userId") Long userId,
                                         @PathVariable("id") Long goodsId,
                                         @Valid @RequestBody UpdateGoodsRequest request) {
        goodsService.updateGoods(userId, goodsId, request);
        return ApiResponse.success("商品更新成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteGoods(@RequestAttribute("userId") Long userId,
                                         @PathVariable("id") Long goodsId) {
        goodsService.deleteGoods(userId, goodsId);
        return ApiResponse.success("商品删除成功", null);
    }

    @GetMapping("/categories")
    public ApiResponse<List<Category>> getCategories() {
        return ApiResponse.success(goodsService.getCategories());
    }

    @PostMapping("/{id}/favorite")
    public ApiResponse<Void> favoriteGoods(@RequestAttribute("userId") Long userId,
                                           @PathVariable("id") Long goodsId) {
        goodsService.favoriteGoods(userId, goodsId);
        return ApiResponse.success("收藏成功", null);
    }

    @DeleteMapping("/{id}/favorite")
    public ApiResponse<Void> unfavoriteGoods(@RequestAttribute("userId") Long userId,
                                             @PathVariable("id") Long goodsId) {
        goodsService.unfavoriteGoods(userId, goodsId);
        return ApiResponse.success("取消收藏成功", null);
    }

    @GetMapping("/favorites")
    public ApiResponse<List<GoodsResponse>> getFavorites(@RequestAttribute("userId") Long userId) {
        return ApiResponse.success(goodsService.getFavorites(userId));
    }

    @GetMapping("/{goodsId}/reviews")
    public ApiResponse<GoodsReviewResult> getGoodsReviews(@PathVariable("goodsId") Long goodsId) {
        return ApiResponse.success(goodsService.getGoodsReviews(goodsId));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ApiResponse<Void> deleteReview(@PathVariable("reviewId") Long reviewId) {
        goodsService.deleteReview(reviewId);
        return ApiResponse.success("评价已删除", null);
    }

    @DeleteMapping("/reviews/order/{orderId}")
    public ApiResponse<Void> deleteReviewByOrder(@PathVariable("orderId") Long orderId) {
        goodsService.deleteReviewByOrder(orderId);
        return ApiResponse.success("评价已删除", null);
    }

    @GetMapping("/first-images")
    public ApiResponse<Map<Long, String>> getFirstImages(@RequestParam("ids") List<Long> ids) {
        return ApiResponse.success(goodsService.getFirstImages(ids));
    }
}