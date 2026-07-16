package com.secondhand.mapper;

import com.secondhand.entity.News;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface NewsMapper {
    News findById(@Param("newsId") Long newsId);
    List<News> findPage(@Param("offset") int offset, @Param("limit") int limit);
    List<News> findByCategoryId(@Param("categoryId") Long categoryId);
    List<News> findAll();
    int insert(News news);
    int update(News news);
    int updateHits(@Param("newsId") Long newsId);
    int deleteById(@Param("newsId") Long newsId);
}