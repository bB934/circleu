package com.secondhand.content.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.secondhand.common.BusinessException;
import com.secondhand.common.PageResult;
import com.secondhand.dto.response.AdminStatsResponse;
import com.secondhand.entity.Goods;
import com.secondhand.entity.Order;
import com.secondhand.entity.User;
import com.secondhand.feign.StockFeignClient;
import com.secondhand.mapper.GoodsMapper;
import com.secondhand.mapper.OrderMapper;
import com.secondhand.mapper.UserMapper;
import com.secondhand.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;
    private final OrderMapper orderMapper;
    private final StockFeignClient stockFeignClient;

    @Override
    public AdminStatsResponse getStats() {
        AdminStatsResponse response = new AdminStatsResponse();
        List<User> allUsers = userMapper.findAll();
        response.setTotalUsers((long) allUsers.size());
        response.setTotalGoods((long) goodsMapper.findAll().size());
        response.setTotalOrders((long) orderMapper.findAll().size());
        response.setPendingGoods((long) goodsMapper.findPage(null, null, null, null, 0, null, 0, Integer.MAX_VALUE).size());
        response.setPendingOrders((long) orderMapper.findByStatus("待付款", null, null, 0, Integer.MAX_VALUE).size());
        return response;
    }

    @Override
    public PageResult<User> getUserList(String keyword, String role, int page, int size) {
        PageHelper.startPage(page, size);
        List<User> users = userMapper.findPage(keyword, role, (page - 1) * size, size);
        PageInfo<User> pageInfo = new PageInfo<>(users);
        return new PageResult<>(users, pageInfo.getTotal(), page, size);
    }

    @Override
    public void auditUser(Long userId, Boolean passed) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setExamineState(passed ? "已通过" : "已拒绝");
        userMapper.update(user);
    }

    @Override
    public void deleteUser(Long userId) {
        userMapper.deleteById(userId);
    }

    @Override
    public PageResult<Goods> getGoodsList(String keyword, Integer status, int page, int size) {
        PageHelper.startPage(page, size);
        List<Goods> goods = goodsMapper.findPage(keyword, null, null, null, status, null, (page - 1) * size, size);
        PageInfo<Goods> pageInfo = new PageInfo<>(goods);
        return new PageResult<>(goods, pageInfo.getTotal(), page, size);
    }

    @Override
    public void auditGoods(Long goodsId, Boolean passed) {
        Goods goods = goodsMapper.findById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        goods.setStatus(passed ? 1 : 0);
        goodsMapper.update(goods);
    }

    @Override
    public void deleteGoods(Long goodsId) {
        goodsMapper.deleteById(goodsId);
    }

    @Override
    public PageResult<Order> getOrderList(String status, int page, int size) {
        PageHelper.startPage(page, size);
        List<Order> orders = orderMapper.findByStatus(status, null, null, (page - 1) * size, size);
        PageInfo<Order> pageInfo = new PageInfo<>(orders);
        return new PageResult<>(orders, pageInfo.getTotal(), page, size);
    }

    @Override
    @Transactional
    public void forceRefund(Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"待发货".equals(order.getStatus()) && !"待付款".equals(order.getStatus())) {
            throw new BusinessException("该订单无法退款");
        }
        Goods goods = goodsMapper.findById(order.getGoodsId());
        if (goods != null) {
            goods.setInventory(goods.getInventory() + order.getNum());
            goodsMapper.update(goods);
            stockFeignClient.increaseStock(order.getGoodsId(), order.getNum());
        }
        order.setStatus("已取消");
        orderMapper.update(order);
    }
}