package com.secondhand.mapper;

import com.secondhand.entity.Favorite;
import com.secondhand.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FavoriteMapper {
    boolean exists(@Param("userId") Long userId, @Param("goodsId") Long goodsId);
    List<Favorite> findByUserId(@Param("userId") Long userId);
    List<Goods> findFavoritesByUserId(@Param("userId") Long userId);
    int insert(Favorite favorite);
    int deleteByUserIdAndGoodsId(@Param("userId") Long userId, @Param("goodsId") Long goodsId);
    int deleteByGoodsId(@Param("goodsId") Long goodsId);
}