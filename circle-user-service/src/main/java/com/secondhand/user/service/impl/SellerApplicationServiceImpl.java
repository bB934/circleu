package com.secondhand.user.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.secondhand.common.BusinessException;
import com.secondhand.common.PageResult;
import com.secondhand.dto.request.ApplySellerRequest;
import com.secondhand.dto.request.AuditApplicationRequest;
import com.secondhand.entity.SellerApplication;
import com.secondhand.entity.Seller;
import com.secondhand.entity.User;
import com.secondhand.mapper.SellerApplicationMapper;
import com.secondhand.mapper.SellerMapper;
import com.secondhand.mapper.UserMapper;
import com.secondhand.service.SellerApplicationService;
import com.secondhand.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SellerApplicationServiceImpl implements SellerApplicationService {

    private final SellerApplicationMapper applicationMapper;
    private final UserMapper userMapper;
    private final SellerMapper sellerMapper;
    private final IdGenerator idGenerator;

    @Override
    @Transactional
    public void apply(Long userId, ApplySellerRequest request) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("seller".equals(user.getRole()) || "admin".equals(user.getRole())) {
            throw new BusinessException("您已是卖家，无需重复申请");
        }

        SellerApplication existing = applicationMapper.findByUserIdAndStatus(userId, "pending");
        if (existing != null) {
            throw new BusinessException("您已有待审核的申请，请耐心等待");
        }

        SellerApplication application = new SellerApplication();
        application.setUserId(userId);
        application.setRealName(request.getRealName());
        application.setPhone(request.getPhone());
        application.setGender(request.getGender());
        application.setAge(request.getAge());
        application.setSchool(request.getSchool());
        application.setAddress(request.getAddress());
        application.setBirthday(request.getBirthday());
        application.setIntroduction(request.getIntroduction());
        application.setStatus("pending");
        applicationMapper.insert(application);
    }

    @Override
    public Map<String, Object> getStatus(Long userId) {
        Map<String, Object> result = new HashMap<>();
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if ("seller".equals(user.getRole()) || "admin".equals(user.getRole())) {
            result.put("status", "approved");
            result.put("message", "您已是卖家");
            return result;
        }

        SellerApplication application = applicationMapper.findByUserId(userId);
        if (application == null) {
            result.put("status", "none");
            result.put("message", "尚未申请");
        } else {
            result.put("status", application.getStatus());
            result.put("application", application);
            if ("pending".equals(application.getStatus())) {
                result.put("message", "申请审核中，请耐心等待");
            } else if ("rejected".equals(application.getStatus())) {
                result.put("message", "申请已拒绝，可重新申请");
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void cancelApply(Long userId) {
        applicationMapper.deleteByUserId(userId);
    }

    @Override
    public PageResult<SellerApplication> getApplicationList(String status, String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        List<SellerApplication> list = applicationMapper.findPage(status, keyword);
        PageInfo<SellerApplication> pageInfo = new PageInfo<>(list);
        return new PageResult<>(list, pageInfo.getTotal(), page, size);
    }

    @Override
    @Transactional
    public void audit(Long applicationId, AuditApplicationRequest request) {
        SellerApplication application = applicationMapper.findById(applicationId);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }
        if (!"pending".equals(application.getStatus())) {
            throw new BusinessException("该申请已处理");
        }

        String newStatus = request.getPassed() ? "approved" : "rejected";
        application.setStatus(newStatus);
        application.setRemark(request.getRemark());
        applicationMapper.update(application);

        if (request.getPassed()) {
            User user = userMapper.findById(application.getUserId());
            user.setRole("seller");
            user.setExamineState("已通过");
            userMapper.update(user);

            Seller seller = new Seller();
            seller.setUserId(user.getUserId());
            seller.setSellerNumber(idGenerator.generateSellerNumber());
            seller.setSellerGender(application.getGender());
            seller.setSellerAge(application.getAge());
            seller.setSellerSchool(application.getSchool());
            seller.setSellerAddress(application.getAddress());
            seller.setSellerBirthday(application.getBirthday());
            seller.setBriefIntroduction(application.getIntroduction());
            seller.setExamineState("已通过");
            seller.setCreditScore(0);
            sellerMapper.insert(seller);
        }
    }
}