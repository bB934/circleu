package com.secondhand.mapper;

import com.secondhand.entity.SignInScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SignInScoreMapper {
    SignInScore findById(@Param("signInScoreId") Long signInScoreId);
    List<SignInScore> findByOrderNumber(@Param("orderNumber") String orderNumber);
    List<SignInScore> findByBusiness(@Param("business") Long business);
    int insert(SignInScore signInScore);
    int update(SignInScore signInScore);
}