package com.secondhand.mapper;

import com.secondhand.entity.Order;
import com.secondhand.vo.GoodsReviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper {
    Order findById(@Param("orderId") Long orderId);
    Order findByOrderNumber(@Param("orderNumber") String orderNumber);
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId,
                                        @Param("status") String status,
                                        @Param("keyword") String keyword,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);
    List<Order> findByStatus(@Param("status") String status,
                              @Param("merchantId") Long merchantId,
                              @Param("keyword") String keyword,
                              @Param("offset") int offset,
                              @Param("limit") int limit);
    List<Order> findAll();
    int insert(Order order);
    int update(Order order);
    int deleteById(@Param("orderId") Long orderId);

    List<GoodsReviewVO> selectGoodsReviews(@Param("goodsId") Long goodsId);
}