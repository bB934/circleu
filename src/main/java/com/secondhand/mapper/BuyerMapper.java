package com.secondhand.mapper;

import com.secondhand.entity.Buyer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BuyerMapper {
    Buyer findById(@Param("buyersId") Long buyersId);
    Buyer findByUserId(@Param("userId") Long userId);
    List<Buyer> findAll();
    int insert(Buyer buyer);
    int update(Buyer buyer);
    int deleteById(@Param("buyersId") Long buyersId);
}