package com.secondhand.mapper;

import com.secondhand.entity.SellerApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SellerApplicationMapper {
    SellerApplication findById(@Param("applicationId") Long applicationId);
    SellerApplication findByUserId(@Param("userId") Long userId);
    SellerApplication findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    List<SellerApplication> findPage(@Param("status") String status, @Param("keyword") String keyword);
    int countPage(@Param("status") String status, @Param("keyword") String keyword);
    int insert(SellerApplication application);
    int update(SellerApplication application);
    int deleteByUserId(@Param("userId") Long userId);
}