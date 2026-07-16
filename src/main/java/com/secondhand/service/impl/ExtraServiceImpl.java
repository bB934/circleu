package com.secondhand.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.secondhand.common.BusinessException;
import com.secondhand.common.PageResult;
import com.secondhand.dto.request.AddAnnouncementRequest;
import com.secondhand.dto.request.AddCarouselRequest;
import com.secondhand.dto.request.AddCommentRequest;
import com.secondhand.dto.request.AddForumPostRequest;
import com.secondhand.dto.response.*;
import com.secondhand.entity.*;
import com.secondhand.mapper.*;
import com.secondhand.service.ExtraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExtraServiceImpl implements ExtraService {

    private final AnnouncementMapper announcementMapper;
    private final CarouselMapper carouselMapper;
    private final NewsMapper newsMapper;
    private final NewsCategoryMapper newsCategoryMapper;
    private final CommentMapper commentMapper;
    private final ForumPostMapper forumPostMapper;
    private final UserMapper userMapper;

    @Override
    public List<AnnouncementResponse> getAnnouncements() {
        List<Announcement> list = announcementMapper.findByStatus(1);
        return list.stream().map(AnnouncementResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    public void addAnnouncement(AddAnnouncementRequest request) {
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setStatus(1);
        announcementMapper.insert(announcement);
    }

    @Override
    public List<CarouselResponse> getCarousels() {
        List<Carousel> list = carouselMapper.findByStatus(1);
        return list.stream().map(CarouselResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    public void addCarousel(AddCarouselRequest request) {
        Carousel carousel = new Carousel();
        carousel.setTitle(request.getTitle());
        carousel.setImageUrl(request.getImageUrl());
        carousel.setLinkUrl(request.getLinkUrl());
        carousel.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        carousel.setStatus(1);
        carouselMapper.insert(carousel);
    }

    @Override
    public PageResult<NewsResponse> getNewsList(int page, int size) {
        PageHelper.startPage(page, size);
        List<News> list = newsMapper.findPage((page - 1) * size, size);
        PageInfo<News> pageInfo = new PageInfo<>(list);
        List<NewsResponse> responses = list.stream()
                .map(NewsResponse::fromEntity)
                .collect(Collectors.toList());
        return new PageResult<>(responses, pageInfo.getTotal(), page, size);
    }

    @Override
    public NewsResponse getNewsDetail(Long newsId) {
        News news = newsMapper.findById(newsId);
        if (news == null) {
            throw new BusinessException("资讯不存在");
        }
        newsMapper.updateHits(newsId);
        return NewsResponse.fromEntity(news);
    }

    @Override
    public List<CommentResponse> getComments(Long goodsId) {
        List<Comment> list = commentMapper.findBySourceId(goodsId);
        return list.stream()
                .map(comment -> {
                    CommentResponse response = CommentResponse.fromEntity(comment);
                    User user = userMapper.findById(comment.getUserId());
                    if (user != null) {
                        response.setNickname(user.getUsername());
                        response.setAvatar(user.getAvatar());
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addComment(Long userId, AddCommentRequest request) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setReplyToId(request.getReplyToId());
        comment.setSourceTable("second_hand_mall");
        comment.setSourceField("second_hand_mall_id");
        comment.setSourceId(request.getSourceId());
        comment.setContent(request.getContent());

        User user = userMapper.findById(userId);
        if (user != null) {
            comment.setNickname(user.getUsername());
            comment.setAvatar(user.getAvatar());
        }
        commentMapper.insert(comment);
    }

    @Override
    public PageResult<ForumPostResponse> getForumPosts(int page, int size) {
        PageHelper.startPage(page, size);
        List<ForumPost> list = forumPostMapper.findPage((page - 1) * size, size);
        PageInfo<ForumPost> pageInfo = new PageInfo<>(list);
        List<ForumPostResponse> responses = list.stream()
                .map(ForumPostResponse::fromEntity)
                .collect(Collectors.toList());
        return new PageResult<>(responses, pageInfo.getTotal(), page, size);
    }

    @Override
    @Transactional
    public void addForumPost(Long userId, AddForumPostRequest request) {
        ForumPost post = new ForumPost();
        post.setUserId(userId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setCategory(request.getCategory());
        post.setStatus(1);
        forumPostMapper.insert(post);
    }
}