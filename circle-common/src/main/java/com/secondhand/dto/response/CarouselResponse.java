package com.secondhand.dto.response;

import com.secondhand.entity.Carousel;
import lombok.Data;

@Data
public class CarouselResponse {
    private Long carouselId;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private Integer sortOrder;

    public static CarouselResponse fromEntity(Carousel carousel) {
        CarouselResponse response = new CarouselResponse();
        response.setCarouselId(carousel.getCarouselId());
        response.setTitle(carousel.getTitle());
        response.setImageUrl(carousel.getImageUrl());
        response.setLinkUrl(carousel.getLinkUrl());
        response.setSortOrder(carousel.getSortOrder());
        return response;
    }
}