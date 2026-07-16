package com.secondhand.controller;

import com.secondhand.common.ApiResponse;
import com.secondhand.common.PageResult;
import com.secondhand.dto.request.AddAnnouncementRequest;
import com.secondhand.dto.request.AddCarouselRequest;
import com.secondhand.dto.request.AddCommentRequest;
import com.secondhand.dto.request.AddForumPostRequest;
import com.secondhand.dto.response.*;
import com.secondhand.service.ExtraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExtraController {

    private final ExtraService extraService;

    @GetMapping("/announcements")
    public ApiResponse<List<AnnouncementResponse>> getAnnouncements() {
        return ApiResponse.success(extraService.getAnnouncements());
    }

    @PostMapping("/admin/announcement")
    public ApiResponse<Void> addAnnouncement(@Valid @RequestBody AddAnnouncementRequest request) {
        extraService.addAnnouncement(request);
        return ApiResponse.success("公告发布成功", null);
    }

    @GetMapping("/carousels")
    public ApiResponse<List<CarouselResponse>> getCarousels() {
        return ApiResponse.success(extraService.getCarousels());
    }

    @PostMapping("/admin/carousel")
    public ApiResponse<Void> addCarousel(@Valid @RequestBody AddCarouselRequest request) {
        extraService.addCarousel(request);
        return ApiResponse.success("轮播图添加成功", null);
    }

    @GetMapping("/news/list")
    public ApiResponse<PageResult<NewsResponse>> getNewsList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(extraService.getNewsList(page, size));
    }

    @GetMapping("/news/{id}")
    public ApiResponse<NewsResponse> getNewsDetail(@PathVariable("id") Long newsId) {
        return ApiResponse.success(extraService.getNewsDetail(newsId));
    }

    @GetMapping("/goods/{goodsId}/comments")
    public ApiResponse<List<CommentResponse>> getComments(@PathVariable("goodsId") Long goodsId) {
        return ApiResponse.success(extraService.getComments(goodsId));
    }

    @PostMapping("/comment")
    public ApiResponse<Void> addComment(@RequestAttribute("userId") Long userId,
                                        @Valid @RequestBody AddCommentRequest request) {
        extraService.addComment(userId, request);
        return ApiResponse.success("评论成功", null);
    }

    @GetMapping("/forum_posts")
    public ApiResponse<PageResult<ForumPostResponse>> getForumPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(extraService.getForumPosts(page, size));
    }

    @PostMapping("/forum_post")
    public ApiResponse<Void> addForumPost(@RequestAttribute("userId") Long userId,
                                          @Valid @RequestBody AddForumPostRequest request) {
        extraService.addForumPost(userId, request);
        return ApiResponse.success("发帖成功", null);
    }
}