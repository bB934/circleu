package com.secondhand.feign;

import com.secondhand.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "circle-goods-service")
public interface StockFeignClient {

    @PostMapping("/internal/stock/decrease")
    ApiResponse<Integer> decreaseStock(@RequestParam("goodsId") Long goodsId,
                                        @RequestParam("num") Integer num);

    @PostMapping("/internal/stock/increase")
    ApiResponse<Void> increaseStock(@RequestParam("goodsId") Long goodsId,
                                     @RequestParam("num") Integer num);

    @PostMapping("/internal/stock/sync")
    ApiResponse<Void> syncStockToRedis(@RequestParam("goodsId") Long goodsId);
}