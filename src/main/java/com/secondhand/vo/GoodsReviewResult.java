package com.secondhand.vo;

import lombok.Data;
import java.util.List;

@Data
public class GoodsReviewResult {
    private List<GoodsReviewVO> list;
    private int total;
    private double avgRating;
}
