package com.secondhand.mapper;

import com.secondhand.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface GoodsMapper {
    Goods findById(@Param("goodsId") Long goodsId);
    List<Goods> findAll();
    int insert(Goods goods);
    int update(Goods goods);
    int deleteById(@Param("goodsId") Long goodsId);
    int updateStatus(@Param("goodsId") Long goodsId, @Param("status") Integer status);
    int updateHits(@Param("goodsId") Long goodsId);

    List<Goods> findPage(@Param("keyword") String keyword,
                         @Param("categoryId") Long categoryId,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         @Param("status") Integer status,
                         @Param("sortBy") String sortBy,
                         @Param("offset") int offset,
                         @Param("limit") int limit);

    int countPage(@Param("keyword") String keyword,
                  @Param("categoryId") Long categoryId,
                  @Param("minPrice") BigDecimal minPrice,
                  @Param("maxPrice") BigDecimal maxPrice,
                  @Param("status") Integer status);
    List<Goods> findBySellerId(@Param("sellerId") Long sellerId);
    List<Goods> findRecommend(@Param("limit") int limit);

    List<com.secondhand.vo.SellerCoverVO> selectFirstCovers(@Param("sellerIds") List<Long> sellerIds);

    List<Long> selectAllOnSaleIds();
}