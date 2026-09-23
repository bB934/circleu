package com.secondhand.mapper;

import com.secondhand.entity.ForumPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ForumPostMapper {
    ForumPost findById(@Param("postId") Long postId);
    List<ForumPost> findPage(@Param("offset") int offset, @Param("limit") int limit);
    List<ForumPost> findByCategory(@Param("category") String category);
    List<ForumPost> findByUserId(@Param("userId") Long userId);
    List<ForumPost> findAll();
    int insert(ForumPost post);
    int update(ForumPost post);
    int updateHits(@Param("postId") Long postId);
    int deleteById(@Param("postId") Long postId);
}