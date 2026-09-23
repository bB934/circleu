package com.secondhand.goods.controller;

import com.secondhand.common.ApiResponse;
import com.secondhand.goods.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/stock")
@RequiredArgsConstructor
public class StockInternalController {

    private final StockService stockService;

    @PostMapping("/decrease")
    public ApiResponse<Integer> decreaseStock(@RequestParam Long goodsId, @RequestParam Integer num) {
        int remain = stockService.decreaseStock(goodsId, num);
        return ApiResponse.success(remain);
    }

    @PostMapping("/increase")
    public ApiResponse<Void> increaseStock(@RequestParam Long goodsId, @RequestParam Integer num) {
        stockService.increaseStock(goodsId, num);
        return ApiResponse.success(null);
    }

    @PostMapping("/sync")
    public ApiResponse<Void> syncStockToRedis(@RequestParam Long goodsId) {
        stockService.syncStockToRedis(goodsId);
        return ApiResponse.success(null);
    }
}
