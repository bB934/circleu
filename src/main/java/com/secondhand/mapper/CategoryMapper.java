package com.secondhand.mapper;

import com.secondhand.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CategoryMapper {
    Category findById(@Param("categoryId") Long categoryId);
    List<Category> findAll();
    List<Category> findByParentId(@Param("parentId") Long parentId);
    int insert(Category category);
    int update(Category category);
    int deleteById(@Param("categoryId") Long categoryId);
}