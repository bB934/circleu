package com.secondhand.service;

import com.secondhand.dto.request.*;
import com.secondhand.dto.response.LoginResponse;
import com.secondhand.dto.response.UserInfoResponse;
import com.secondhand.entity.Address;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
    UserInfoResponse getUserInfo(Long userId);
    void updateUserInfo(Long userId, UpdateUserRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    List<Address> getAddressList(Long userId);
    void addAddress(Long userId, AddressRequest request);
    void updateAddress(Long userId, Long addressId, AddressRequest request);
    void deleteAddress(Long userId, Long addressId);
    void setDefaultAddress(Long userId, Long addressId);

    String uploadAvatar(Long userId, MultipartFile file);
}