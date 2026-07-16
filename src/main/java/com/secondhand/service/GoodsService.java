package com.secondhand.service;

import com.secondhand.common.PageResult;
import com.secondhand.dto.request.PublishGoodsRequest;
import com.secondhand.dto.request.UpdateGoodsRequest;
import com.secondhand.dto.response.GoodsResponse;
import com.secondhand.entity.Category;
import com.secondhand.vo.GoodsReviewResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface GoodsService {
    PageResult<GoodsResponse> getGoodsList(String keyword, Long categoryId,
                                           BigDecimal minPrice, BigDecimal maxPrice,
                                           String sortBy, int page, int size);
    GoodsResponse getGoodsDetail(Long goodsId, Long userId);
    void publishGoods(Long userId, PublishGoodsRequest request);
    void updateGoods(Long userId, Long goodsId, UpdateGoodsRequest request);
    void deleteGoods(Long userId, Long goodsId);
    List<Category> getCategories();
    void favoriteGoods(Long userId, Long goodsId);
    void unfavoriteGoods(Long userId, Long goodsId);
    List<GoodsResponse> getFavorites(Long userId);
    Map<Long, String> getFirstImages(List<Long> sellerIds);
    GoodsReviewResult getGoodsReviews(Long goodsId);
    void deleteGoodsReview(Long userId, Long reviewId);
    void deleteGoodsReviewByOrder(Long userId, Long orderId);
}