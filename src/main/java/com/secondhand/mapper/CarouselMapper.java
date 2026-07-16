package com.secondhand.mapper;

import com.secondhand.entity.Carousel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CarouselMapper {
    Carousel findById(@Param("carouselId") Long carouselId);
    List<Carousel> findByStatus(@Param("status") Integer status);
    List<Carousel> findAll();
    int insert(Carousel carousel);
    int update(Carousel carousel);
    int deleteById(@Param("carouselId") Long carouselId);
}