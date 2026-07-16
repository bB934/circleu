package com.secondhand.service.impl;

import com.secondhand.common.BusinessException;
import com.secondhand.dto.request.*;
import com.secondhand.dto.response.LoginResponse;
import com.secondhand.dto.response.UserInfoResponse;
import com.secondhand.entity.Address;
import com.secondhand.entity.Buyer;
import com.secondhand.entity.Seller;
import com.secondhand.entity.User;
import com.secondhand.enums.RoleEnum;
import com.secondhand.mapper.AddressMapper;
import com.secondhand.mapper.BuyerMapper;
import com.secondhand.mapper.SellerMapper;
import com.secondhand.mapper.UserMapper;
import com.secondhand.service.UserService;
import com.secondhand.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BuyerMapper buyerMapper;
    private final SellerMapper sellerMapper;
    private final AddressMapper addressMapper;
    private final JwtUtil jwtUtil;
    private final IdGenerator idGenerator;
    private final FileUploadUtil fileUploadUtil;
    private final AvatarUtil avatarUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setAvatar(user.getAvatar());
        return response;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (userMapper.countByPhone(request.getPhone()) > 0) {
            throw new BusinessException("手机号已注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : RoleEnum.buyer.name());
        user.setExamineState("已通过");
        user.setRecommend(0);

        // ✅ 生成默认头像（基于邮箱）
        String avatar = avatarUtil.generateCravatarAvatar(request.getEmail());
        user.setAvatar(avatar);

        userMapper.insert(user);

        if (RoleEnum.buyer.name().equals(user.getRole())) {
            Buyer buyer = new Buyer();
            buyer.setUserId(user.getUserId());
            buyer.setBuyerNumber(idGenerator.generateBuyerNumber());
            buyer.setExamineState("已通过");
            buyerMapper.insert(buyer);
        } else if (RoleEnum.seller.name().equals(user.getRole())) {
            Seller seller = new Seller();
            seller.setUserId(user.getUserId());
            seller.setSellerNumber(idGenerator.generateSellerNumber());
            seller.setExamineState("待审核");
            sellerMapper.insert(seller);
        }
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setGender(user.getGender());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole());
        response.setExamineState(user.getExamineState());
        return response;
    }

    @Override
    public void updateUserInfo(Long userId, UpdateUserRequest request) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setAvatar(request.getAvatar());
        userMapper.update(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!PasswordUtil.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(PasswordUtil.encode(request.getNewPassword()));
        userMapper.update(user);
    }

    @Override
    public List<Address> getAddressList(Long userId) {
        return addressMapper.findByUserId(userId);
    }

    @Override
    public void addAddress(Long userId, AddressRequest request) {
        Address address = new Address();
        address.setUserId(userId);
        address.setName(request.getName());
        address.setPhone(request.getPhone());
        address.setPostcode(request.getPostcode());
        address.setAddress(request.getAddress());
        address.setIsDefault(request.getIsDefault() != null && request.getIsDefault());
        addressMapper.insert(address);

        if (address.getIsDefault()) {
            addressMapper.clearDefault(userId);
            addressMapper.updateDefault(address.getAddressId());
        }
    }

    @Override
    public void updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = addressMapper.findById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在或无权操作");
        }
        address.setName(request.getName());
        address.setPhone(request.getPhone());
        address.setPostcode(request.getPostcode());
        address.setAddress(request.getAddress());
        address.setIsDefault(request.getIsDefault() != null && request.getIsDefault());
        addressMapper.update(address);

        if (address.getIsDefault()) {
            addressMapper.clearDefault(userId);
            addressMapper.updateDefault(addressId);
        }
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressMapper.findById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在或无权操作");
        }
        addressMapper.deleteById(addressId);
    }

    @Override
    public void setDefaultAddress(Long userId, Long addressId) {
        Address address = addressMapper.findById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在或无权操作");
        }
        addressMapper.clearDefault(userId);
        addressMapper.updateDefault(addressId);
    }

    @Override
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {
        // 1. 检查用户是否存在
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        try {
            // 2. 删除旧头像（如果有）
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                // 只删除本地上传的图片，不删除 Cravatar 等外部链接
                if (user.getAvatar().contains("/uploads/")) {
                    fileUploadUtil.deleteFile(user.getAvatar());
                }
            }

            // 3. 上传新头像
            String avatarUrl = fileUploadUtil.uploadAvatar(userId, file);

            // 4. 更新数据库
            user.setAvatar(avatarUrl);
            userMapper.update(user);

            return avatarUrl;

        } catch (Exception e) {
            throw new BusinessException("头像上传失败: " + e.getMessage());
        }
    }
}