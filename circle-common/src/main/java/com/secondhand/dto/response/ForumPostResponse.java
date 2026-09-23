package com.secondhand.dto.response;

import com.secondhand.entity.ForumPost;
import lombok.Data;

@Data
public class ForumPostResponse {
    private Long postId;
    private Long userId;
    private String username;
    private String title;
    private String content;
    private String category;
    private Integer hits;
    private String createTime;

    public static ForumPostResponse fromEntity(ForumPost post) {
        ForumPostResponse response = new ForumPostResponse();
        response.setPostId(post.getPostId());
        response.setUserId(post.getUserId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setCategory(post.getCategory());
        response.setHits(post.getHits());
        response.setCreateTime(post.getCreateTime() != null ? post.getCreateTime().toString() : null);
        return response;
    }
}