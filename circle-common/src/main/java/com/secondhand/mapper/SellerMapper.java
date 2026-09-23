package com.secondhand.mapper;

import com.secondhand.entity.Seller;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SellerMapper {
    Seller findById(@Param("sellerId") Long sellerId);
    Seller findByUserId(@Param("userId") Long userId);
    int countByUserIdAndNumber(@Param("userId") Long userId, @Param("number") String number);
    List<Seller> findAll();
    int insert(Seller seller);
    int update(Seller seller);
    int deleteById(@Param("sellerId") Long sellerId);
}