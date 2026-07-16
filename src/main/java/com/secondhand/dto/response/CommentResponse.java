package com.secondhand.dto.response;

import com.secondhand.entity.Comment;
import lombok.Data;

@Data
public class CommentResponse {
    private Long commentId;
    private Long userId;
    private String nickname;
    private String avatar;
    private String content;
    private String createTime;

    public static CommentResponse fromEntity(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setCommentId(comment.getCommentId());
        response.setUserId(comment.getUserId());
        response.setContent(comment.getContent());
        response.setCreateTime(comment.getCreateTime() != null ? comment.getCreateTime().toString() : null);
        return response;
    }
}