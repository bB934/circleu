package com.secondhand.dto.response;

import com.secondhand.entity.News;
import lombok.Data;

@Data
public class NewsResponse {
    private Long newsId;
    private String title;
    private String summary;
    private String content;
    private String coverImg;
    private Integer hits;
    private String createTime;

    public static NewsResponse fromEntity(News news) {
        NewsResponse response = new NewsResponse();
        response.setNewsId(news.getNewsId());
        response.setTitle(news.getTitle());
        response.setSummary(news.getSummary());
        response.setContent(news.getContent());
        response.setCoverImg(news.getCoverImg());
        response.setHits(news.getHits());
        response.setCreateTime(news.getCreateTime() != null ? news.getCreateTime().toString() : null);
        return response;
    }
}