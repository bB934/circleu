package com.secondhand.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.secondhand.common.BusinessException;
import com.secondhand.common.PageResult;
import com.secondhand.dto.request.PublishGoodsRequest;
import com.secondhand.dto.request.UpdateGoodsRequest;
import com.secondhand.dto.response.GoodsResponse;
import com.secondhand.entity.*;
import com.secondhand.mapper.*;
import com.secondhand.service.GoodsService;
import com.secondhand.vo.GoodsReviewResult;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodsServiceImpl implements GoodsService {

    private final GoodsMapper goodsMapper;
    private final CategoryMapper categoryMapper;
    private final FavoriteMapper favoriteMapper;
    private final SellerMapper sellerMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final GoodsReviewMapper goodsReviewMapper;
    private final SignInScoreMapper signInScoreMapper;
    private final com.secondhand.util.FileUploadUtil fileUploadUtil;
    private final com.secondhand.service.StockService stockService;
    private final com.secondhand.service.GoodsCacheService goodsCacheService;

    private String saveImage(Long userId, String base64) {
        try {
            return fileUploadUtil.saveBase64Image(userId, base64, "goods");
        } catch (IOException e) {
            throw new BusinessException("图片上传失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult<GoodsResponse> getGoodsList(String keyword, Long categoryId,
                                                   BigDecimal minPrice, BigDecimal maxPrice,
                                                   String sortBy, int page, int size) {
        PageHelper.startPage(page, size);
        List<Goods> goodsList = goodsMapper.findPage(keyword, categoryId, minPrice, maxPrice, 1, sortBy, (page - 1) * size, size);
        PageInfo<Goods> pageInfo = new PageInfo<>(goodsList);
        List<GoodsResponse> responses = goodsList.stream()
                .map(GoodsResponse::fromEntity)
                .collect(Collectors.toList());
        return new PageResult<>(responses, pageInfo.getTotal(), page, size);
    }

    @Override
    @Cacheable(value = "goods", key = "#goodsId", unless = "#result == null")
    public GoodsResponse getGoodsDetail(Long goodsId, Long userId) {
        goodsMapper.updateHits(goodsId);
        // 布隆过滤 + 多级缓存（L1本地/L2 Redis/L3 DB），防穿透与击穿
        Goods goods = goodsCacheService.getGoods(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        GoodsResponse response = GoodsResponse.fromEntity(goods);
        if (userId != null) {
            response.setIsFavorited(favoriteMapper.exists(userId, goodsId));
        }
        return response;
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public void publishGoods(Long userId, PublishGoodsRequest request) {
        Seller seller = sellerMapper.findByUserId(userId);
        if (seller == null) {
            throw new BusinessException("请先申请成为卖家");
        }
        if (!"已通过".equals(seller.getExamineState())) {
            throw new BusinessException("卖家资质未通过审核");
        }

        Goods goods = new Goods();
        goods.setSellerId(seller.getSellerId());
        goods.setCategoryId(request.getCategoryId());
        goods.setTitle(request.getTitle());
        goods.setDescription(request.getDescription());
        goods.setContent(request.getContent());
        goods.setPrice(request.getPrice());
        goods.setPriceAgo(request.getPriceAgo());
        goods.setInventory(request.getInventory() != null ? request.getInventory() : 1);
        goods.setStatus(1);
        goods.setCoverImg(saveImage(userId, request.getCoverImg()));
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<String> images = request.getImages();
            goods.setImg1(images.size() > 0 ? saveImage(userId, images.get(0)) : null);
            goods.setImg2(images.size() > 1 ? saveImage(userId, images.get(1)) : null);
            goods.setImg3(images.size() > 2 ? saveImage(userId, images.get(2)) : null);
            goods.setImg4(images.size() > 3 ? saveImage(userId, images.get(3)) : null);
            goods.setImg5(images.size() > 4 ? saveImage(userId, images.get(4)) : null);
        }
        goodsMapper.insert(goods);
        // 发布成功后预热 Redis 库存，便于后续高并发抢购原子扣减
        stockService.warmUp(goods.getSecondHandMallId(), goods.getInventory(), 1);
        // 加入布隆过滤器，防缓存穿透
        goodsCacheService.addGoods(goods.getSecondHandMallId());
    }

    @Override
    @CacheEvict(value = "goods", key = "#goodsId")
    public void updateGoods(Long userId, Long goodsId, UpdateGoodsRequest request) {
        Goods goods = goodsMapper.findById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        Seller seller = sellerMapper.findByUserId(userId);
        if (seller == null || !seller.getSellerId().equals(goods.getSellerId())) {
            User user = userMapper.findById(userId);
            if (user == null || !"admin".equals(user.getRole())) {
                throw new BusinessException("无权修改该商品");
            }
        }

        goods.setCategoryId(request.getCategoryId());
        goods.setTitle(request.getTitle());
        goods.setDescription(request.getDescription());
        goods.setContent(request.getContent());
        goods.setPrice(request.getPrice());
        goods.setPriceAgo(request.getPriceAgo());
        goods.setInventory(request.getInventory());
        goods.setCoverImg(saveImage(userId, request.getCoverImg()));
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<String> images = request.getImages();
            goods.setImg1(images.size() > 0 ? saveImage(userId, images.get(0)) : null);
            goods.setImg2(images.size() > 1 ? saveImage(userId, images.get(1)) : null);
            goods.setImg3(images.size() > 2 ? saveImage(userId, images.get(2)) : null);
            goods.setImg4(images.size() > 3 ? saveImage(userId, images.get(3)) : null);
            goods.setImg5(images.size() > 4 ? saveImage(userId, images.get(4)) : null);
        }
        goodsMapper.update(goods);
        goodsCacheService.addGoods(goodsId);
        goodsCacheService.refreshStock(goodsId, goods.getInventory());
    }

    @Override
    @CacheEvict(value = "goods", key = "#goodsId")
    @Transactional
    public void deleteGoods(Long userId, Long goodsId) {
        Goods goods = goodsMapper.findById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        Seller seller = sellerMapper.findByUserId(userId);
        if (seller == null || !seller.getSellerId().equals(goods.getSellerId())) {
            User user = userMapper.findById(userId);
            if (user == null || !"admin".equals(user.getRole())) {
                throw new BusinessException("无权删除该商品");
            }
        }
        favoriteMapper.deleteByGoodsId(goodsId);
        goodsMapper.deleteById(goodsId);
        // 失效布隆/多级缓存
        goodsCacheService.removeGoods(goodsId);
    }

    @Override
    @Cacheable(value = "categories")
    public List<Category> getCategories() {
        return categoryMapper.findAll();
    }

    @Override
    public void favoriteGoods(Long userId, Long goodsId) {
        if (favoriteMapper.exists(userId, goodsId)) {
            throw new BusinessException("已收藏该商品");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setGoodsId(goodsId);
        favoriteMapper.insert(favorite);
    }

    @Override
    public void unfavoriteGoods(Long userId, Long goodsId) {
        if (!favoriteMapper.exists(userId, goodsId)) {
            throw new BusinessException("未收藏该商品");
        }
        favoriteMapper.deleteByUserIdAndGoodsId(userId, goodsId);
    }

    @Override
    public List<GoodsResponse> getFavorites(Long userId) {
        List<Goods> goodsList = favoriteMapper.findFavoritesByUserId(userId);
        return goodsList.stream()
                .map(GoodsResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, String> getFirstImages(List<Long> sellerIds) {
        if (sellerIds == null || sellerIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<com.secondhand.vo.SellerCoverVO> list = goodsMapper.selectFirstCovers(sellerIds);
        Map<Long, String> map = new HashMap<>();
        for (com.secondhand.vo.SellerCoverVO v : list) {
            map.put(v.getSellerId(), v.getCoverImg());
        }
        return map;
    }

    @Override
    public GoodsReviewResult getGoodsReviews(Long goodsId) {
        List<com.secondhand.vo.GoodsReviewVO> list = orderMapper.selectGoodsReviews(goodsId);
        GoodsReviewResult result = new GoodsReviewResult();
        result.setList(list);
        result.setTotal(list.size());
        double avg = list.stream()
                .filter(v -> v.getRating() != null)
                .mapToInt(com.secondhand.vo.GoodsReviewVO::getRating)
                .average()
                .orElse(0.0);
        result.setAvgRating(Math.round(avg * 10.0) / 10.0);
        return result;
    }

    @Override
    @Transactional
    public void deleteGoodsReview(Long userId, Long reviewId) {
        GoodsReview review = goodsReviewMapper.selectById(reviewId);
        deleteReviewInternal(userId, review);
    }

    @Override
    @Transactional
    public void deleteGoodsReviewByOrder(Long userId, Long orderId) {
        GoodsReview review = goodsReviewMapper.selectByOrderId(orderId);
        deleteReviewInternal(userId, review);
    }

    private void deleteReviewInternal(Long userId, GoodsReview review) {
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的评价");
        }
        // 回退订单与签到分，保持双写一致：订单回到“已完成”，清空评分/备注
        Order order = orderMapper.findById(review.getOrderId());
        if (order != null) {
            order.setStatus("已完成");
            order.setStarRating(null);
            order.setRemarks(null);
            orderMapper.update(order);

            List<SignInScore> scores = signInScoreMapper.findByOrderNumber(order.getOrderNumber());
            if (scores != null) {
                for (SignInScore score : scores) {
                    score.setStarRating(null);
                    score.setRemarks(null);
                    signInScoreMapper.update(score);
                }
            }
        }
        goodsReviewMapper.deleteById(review.getReviewId());
    }
}