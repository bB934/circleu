package com.secondhand.controller;

import com.secondhand.common.ApiResponse;
import com.secondhand.common.PageResult;
import com.secondhand.dto.request.PublishGoodsRequest;
import com.secondhand.dto.request.UpdateGoodsRequest;
import com.secondhand.dto.response.GoodsResponse;
import com.secondhand.entity.Category;
import com.secondhand.service.GoodsService;
import com.secondhand.vo.GoodsReviewResult;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
@Slf4j
public class GoodsController {

    private final GoodsService goodsService;

    @GetMapping("/list")
    @SentinelResource(value = "goodsResource:list", blockHandler = "listBlockHandler")
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

    public ApiResponse<PageResult<GoodsResponse>> listBlockHandler(
            String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            String sortBy, int page, int size, BlockException ex) {
        log.warn("商品检索被Sentinel限流: {}", ex.getMessage());
        return ApiResponse.error("商品检索请求过于频繁，请稍后重试");
    }

    @GetMapping("/{id}")
    @SentinelResource(value = "goodsResource:detail", blockHandler = "detailBlockHandler",
            fallback = "detailFallback")
    public ApiResponse<GoodsResponse> getGoodsDetail(@PathVariable("id") Long goodsId,
                                                     @RequestAttribute(value = "userId", required = false) Long userId) {
        return ApiResponse.success(goodsService.getGoodsDetail(goodsId, userId));
    }

    public ApiResponse<GoodsResponse> detailBlockHandler(Long goodsId, Long userId, BlockException ex) {
        log.warn("商品详情被Sentinel熔断/限流: goodsId={}", goodsId);
        return ApiResponse.error("商品服务繁忙，请稍后重试");
    }

    public ApiResponse<GoodsResponse> detailFallback(Long goodsId, Long userId, Throwable t) {
        log.warn("商品详情降级: goodsId={}, err={}", goodsId, t.getMessage());
        return ApiResponse.error("商品详情暂时不可用，请稍后重试");
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

    @DeleteMapping("/reviews/{id}")
    public ApiResponse<Void> deleteGoodsReview(@RequestAttribute("userId") Long userId,
                                              @PathVariable("id") Long reviewId) {
        goodsService.deleteGoodsReview(userId, reviewId);
        return ApiResponse.success("删除评价成功", null);
    }

    @DeleteMapping("/reviews/order/{orderId}")
    public ApiResponse<Void> deleteGoodsReviewByOrder(@RequestAttribute("userId") Long userId,
                                                     @PathVariable("orderId") Long orderId) {
        goodsService.deleteGoodsReviewByOrder(userId, orderId);
        return ApiResponse.success("删除评价成功", null);
    }
}