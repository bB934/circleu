package com.secondhand.service;

import com.secondhand.dto.request.UpdateSellerRequest;
import com.secondhand.entity.Seller;

public interface SellerService {
    Seller getInfo(Long userId);
    Seller updateInfo(Long userId, UpdateSellerRequest request);
}