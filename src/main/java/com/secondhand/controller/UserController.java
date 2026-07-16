package com.secondhand.controller;

import com.secondhand.common.ApiResponse;
import com.secondhand.dto.request.*;
import com.secondhand.dto.response.LoginResponse;
import com.secondhand.dto.response.UserInfoResponse;
import com.secondhand.entity.Address;
import com.secondhand.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ApiResponse.success("注册成功", null);
    }

    @GetMapping("/info")
    public ApiResponse<UserInfoResponse> getUserInfo(@RequestAttribute("userId") Long userId) {
        return ApiResponse.success(userService.getUserInfo(userId));
    }

    @PutMapping("/info")
    public ApiResponse<Void> updateUserInfo(@RequestAttribute("userId") Long userId,
                                            @RequestBody UpdateUserRequest request) {
        userService.updateUserInfo(userId, request);
        return ApiResponse.success("更新成功", null);
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestAttribute("userId") Long userId,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userId, request);
        return ApiResponse.success("密码修改成功", null);
    }

    @GetMapping("/addresses")
    public ApiResponse<List<Address>> getAddressList(@RequestAttribute("userId") Long userId) {
        return ApiResponse.success(userService.getAddressList(userId));
    }

    @PostMapping("/address")
    public ApiResponse<Void> addAddress(@RequestAttribute("userId") Long userId,
                                        @Valid @RequestBody AddressRequest request) {
        userService.addAddress(userId, request);
        return ApiResponse.success("地址添加成功", null);
    }

    @PutMapping("/address/{id}")
    public ApiResponse<Void> updateAddress(@RequestAttribute("userId") Long userId,
                                           @PathVariable("id") Long addressId,
                                           @Valid @RequestBody AddressRequest request) {
        userService.updateAddress(userId, addressId, request);
        return ApiResponse.success("地址更新成功", null);
    }

    @DeleteMapping("/address/{id}")
    public ApiResponse<Void> deleteAddress(@RequestAttribute("userId") Long userId,
                                           @PathVariable("id") Long addressId) {
        userService.deleteAddress(userId, addressId);
        return ApiResponse.success("地址删除成功", null);
    }

    @PutMapping("/address/{id}/default")
    public ApiResponse<Void> setDefaultAddress(@RequestAttribute("userId") Long userId,
                                               @PathVariable("id") Long addressId) {
        userService.setDefaultAddress(userId, addressId);
        return ApiResponse.success("设置默认地址成功", null);
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ApiResponse<String> uploadAvatar(
            @RequestAttribute("userId") Long userId,
            @RequestParam("file") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(userId, file);
        return ApiResponse.success("头像上传成功", avatarUrl);
    }
}