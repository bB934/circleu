package com.secondhand.feign;

import com.secondhand.common.ApiResponse;
import com.secondhand.entity.Goods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "circle-goods-service")
public interface GoodsFeignClient {

    @GetMapping("/internal/goods/{goodsId}")
    ApiResponse<Goods> getGoodsById(@PathVariable("goodsId") Long goodsId);

    @GetMapping("/internal/goods/{goodsId}/sellerId")
    ApiResponse<Long> getSellerId(@PathVariable("goodsId") Long goodsId);
}