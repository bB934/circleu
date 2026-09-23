package com.secondhand.mapper;

import com.secondhand.entity.NewsCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NewsCategoryMapper {
    NewsCategory findById(@Param("categoryId") Long categoryId);
    List<NewsCategory> findAll();
    int insert(NewsCategory category);
    int update(NewsCategory category);
    int deleteById(@Param("categoryId") Long categoryId);
}