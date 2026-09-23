package com.secondhand.feign;

import com.secondhand.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "circle-goods-service")
public interface GoodsServiceClient {

    @GetMapping("/goods/first-images")
    ApiResponse<Map<Long, String>> getFirstImages(@RequestParam("ids") List<Long> ids);
}
