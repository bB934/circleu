package com.secondhand.feign;

import com.secondhand.common.ApiResponse;
import com.secondhand.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "circle-user-service")
public interface UserFeignClient {

    @GetMapping("/internal/user/{userId}")
    ApiResponse<User> getUserById(@PathVariable("userId") Long userId);
}