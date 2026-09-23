package com.secondhand.mapper;

import com.secondhand.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CartMapper {
    Cart findById(@Param("cartId") Long cartId);
    Cart findByUserIdAndGoodsId(@Param("userId") Long userId, @Param("goodsId") Long goodsId);
    List<Cart> findByUserId(@Param("userId") Long userId);
    List<Cart> findByIds(@Param("ids") List<Long> ids);
    int insert(Cart cart);
    int update(Cart cart);
    int deleteById(@Param("cartId") Long cartId);
    int deleteByUserId(@Param("userId") Long userId);
}