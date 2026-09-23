package com.secondhand.user.controller;

import com.secondhand.common.ApiResponse;
import com.secondhand.entity.User;
import com.secondhand.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/user")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserMapper userMapper;

    @GetMapping("/{userId}")
    public ApiResponse<User> getUserById(@PathVariable Long userId) {
        User user = userMapper.findById(userId);
        return ApiResponse.success(user);
    }
}