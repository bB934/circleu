package com.secondhand.mapper;

import com.secondhand.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CommentMapper {
    Comment findById(@Param("commentId") Long commentId);
    List<Comment> findBySourceId(@Param("sourceId") Long sourceId);
    List<Comment> findByUserId(@Param("userId") Long userId);
    int insert(Comment comment);
    int update(Comment comment);
    int deleteById(@Param("commentId") Long commentId);
}