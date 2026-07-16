package com.secondhand.service;

import com.secondhand.common.PageResult;
import com.secondhand.dto.request.AddAnnouncementRequest;
import com.secondhand.dto.request.AddCarouselRequest;
import com.secondhand.dto.request.AddCommentRequest;
import com.secondhand.dto.request.AddForumPostRequest;
import com.secondhand.dto.response.*;

import java.util.List;

public interface ExtraService {
    List<AnnouncementResponse> getAnnouncements();
    void addAnnouncement(AddAnnouncementRequest request);
    List<CarouselResponse> getCarousels();
    void addCarousel(AddCarouselRequest request);
    PageResult<NewsResponse> getNewsList(int page, int size);
    NewsResponse getNewsDetail(Long newsId);
    List<CommentResponse> getComments(Long goodsId);
    void addComment(Long userId, AddCommentRequest request);
    PageResult<ForumPostResponse> getForumPosts(int page, int size);
    void addForumPost(Long userId, AddForumPostRequest request);
}