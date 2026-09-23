package com.secondhand.user.service.impl;

import com.secondhand.common.BusinessException;
import com.secondhand.dto.request.UpdateSellerRequest;
import com.secondhand.entity.Seller;
import com.secondhand.mapper.SellerMapper;
import com.secondhand.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final SellerMapper sellerMapper;

    @Override
    public Seller getInfo(Long userId) {
        Seller seller = sellerMapper.findByUserId(userId);
        if (seller == null) {
            throw new BusinessException("卖家信息不存在");
        }
        return seller;
    }

    @Override
    @Transactional
    public Seller updateInfo(Long userId, UpdateSellerRequest request) {
        Seller seller = sellerMapper.findByUserId(userId);
        if (seller == null) {
            throw new BusinessException("卖家信息不存在");
        }
        // 仅允许更新可填字段，不可改 seller_number / credit_score / examine_state
        if (request.getSellerGender() != null) {
            seller.setSellerGender(request.getSellerGender());
        }
        if (request.getSellerAge() != null) {
            seller.setSellerAge(request.getSellerAge());
        }
        if (request.getSellerSchool() != null) {
            seller.setSellerSchool(request.getSellerSchool());
        }
        if (request.getSellerAddress() != null) {
            seller.setSellerAddress(request.getSellerAddress());
        }
        if (request.getSellerBirthday() != null) {
            seller.setSellerBirthday(request.getSellerBirthday());
        }
        if (request.getBriefIntroduction() != null) {
            seller.setBriefIntroduction(request.getBriefIntroduction());
        }
        sellerMapper.update(seller);
        return seller;
    }
}