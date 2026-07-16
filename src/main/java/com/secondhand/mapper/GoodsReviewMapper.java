package com.secondhand.mapper;

import com.secondhand.entity.GoodsReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GoodsReviewMapper {
    GoodsReview selectByOrderId(@Param("orderId") Long orderId);
    GoodsReview selectById(@Param("reviewId") Long reviewId);
    int insert(GoodsReview review);
    int updateByOrderId(GoodsReview review);
    int deleteById(@Param("reviewId") Long reviewId);
}
