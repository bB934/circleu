package com.secondhand.order.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.secondhand.common.BusinessException;
import com.secondhand.common.PageResult;
import com.secondhand.dto.request.CreateOrderRequest;
import com.secondhand.dto.request.RateOrderRequest;
import com.secondhand.dto.response.OrderResponse;
import com.secondhand.entity.*;
import com.secondhand.feign.StockFeignClient;
import com.secondhand.mapper.*;
import com.secondhand.mq.OrderDelayMessage;
import com.secondhand.order.mq.RocketMQOrderProducer;
import com.secondhand.service.OrderService;
import com.secondhand.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final CartMapper cartMapper;
    private final GoodsMapper goodsMapper;
    private final AddressMapper addressMapper;
    private final SignInScoreMapper signInScoreMapper;
    private final StockFeignClient stockFeignClient;
    private final RocketMQOrderProducer rocketMQOrderProducer;
    private final GoodsReviewMapper goodsReviewMapper;
    private final IdGenerator idGenerator;

    @Override
    @Transactional
    @SentinelResource(value = "createOrder", blockHandler = "createOrderBlock", fallback = "createOrderFallback")
    public Order createOrder(Long userId, CreateOrderRequest request) {
        List<Cart> cartList;
        if (request.getCartIds() != null && !request.getCartIds().isEmpty()) {
            cartList = cartMapper.findByIds(request.getCartIds());
        } else {
            cartList = cartMapper.findByUserId(userId);
        }
        if (cartList.isEmpty()) {
            throw new BusinessException("购物车为空");
        }

        Address address = addressMapper.findById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("收货地址不存在");
        }

        // 记录本次事务中已扣减的库存，供事务回滚时补偿（Redis 扣减不参与 DB 事务，必须手工回补）
        List<StockDeduction> deductions = new ArrayList<>();
        registerStockRollbackCompensation(deductions);

        Order firstOrder = null;
        for (Cart cart : cartList) {
            Goods goods = goodsMapper.findById(cart.getGoodsId());
            if (goods == null || goods.getStatus() != 1) {
                throw new BusinessException("商品已下架或已售出: " + cart.getGoodsId());
            }

            stockFeignClient.decreaseStock(goods.getSecondHandMallId(), cart.getNum());
            deductions.add(new StockDeduction(goods.getSecondHandMallId(), cart.getNum()));

            Order order = new Order();
            order.setOrderNumber(idGenerator.generateOrderNumber());
            order.setUserId(userId);
            order.setMerchantId(goods.getSellerId());
            order.setGoodsId(goods.getSecondHandMallId());
            order.setTitle(goods.getTitle());
            order.setImg(goods.getCoverImg());
            order.setPrice(cart.getPrice());
            order.setPriceAgo(cart.getPriceAgo());
            order.setNum(cart.getNum());
            order.setPriceCount(cart.getPrice().multiply(BigDecimal.valueOf(cart.getNum())));
            order.setNorms(goods.getRemarks());
            order.setType("");
            order.setDescription(request.getRemark());
            order.setContactName(address.getName());
            order.setContactPhone(address.getPhone());
            order.setContactAddress(address.getAddress());
            order.setPostalCode(address.getPostcode());
            order.setStatus("待付款");
            orderMapper.insert(order);

            cartMapper.deleteById(cart.getCartId());

            // 下单后立即同步商品上下架状态：可售库存归零则置为已售，避免"上架但库存为0"
            syncGoodsStatus(order.getGoodsId());

            rocketMQOrderProducer.sendDelayMessage(order, 15 * 60 * 1000L);

            if (firstOrder == null) {
                firstOrder = order;
            }
        }
        return firstOrder;
    }

    /**
     * 注册事务完成回调：若 DB 事务回滚，把本次已扣减的 Redis 库存补回去。
     * 解决"一车多件时后续商品失败导致前几件库存被永久扣掉"的补偿缺口。
     */
    private void registerStockRollbackCompensation(List<StockDeduction> deductions) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    for (StockDeduction d : deductions) {
                        try {
                            stockFeignClient.increaseStock(d.goodsId, d.num);
                            log.warn("下单事务回滚，已补偿回补库存: goodsId={}, num={}", d.goodsId, d.num);                        } catch (Exception e) {
                            log.error("下单事务回滚后补偿库存失败，需人工介入: goodsId={}, num={}",
                                    d.goodsId, d.num, e);
                        }
                    }
                }
            }
        });
    }

    /** 库存扣减记录（仅用于事务回滚补偿） */
    private static class StockDeduction {
        private final Long goodsId;
        private final Integer num;

        StockDeduction(Long goodsId, Integer num) {
            this.goodsId = goodsId;
            this.num = num;
        }
    }

    /**
     * 根据"剩余库存 - 未完结订单占用库存"同步商品上下架状态。
     * 可售库存归零 → 已售(2)；可售库存恢复（如订单取消回补）→ 重新上架(1)。
     * 注意：本方法只改 status，不动 inventory，因此不会与 Redis/Lua 的扣减产生冲突。
     */
    private void syncGoodsStatus(Long goodsId) {
        Goods goods = goodsMapper.findById(goodsId);
        if (goods == null) {
            return;
        }
        int inventory = goods.getInventory() != null ? goods.getInventory() : 0;
        int occupied = orderMapper.sumPendingStock(goodsId);
        int available = inventory - occupied;
        goodsMapper.updateStatus(goodsId, available <= 0 ? 2 : 1);
    }

    @Override
    public PageResult<OrderResponse> getOrderList(Long userId, String status, String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        List<Order> orders = orderMapper.findByUserIdAndStatus(userId, status, keyword, (page - 1) * size, size);
        PageInfo<Order> pageInfo = new PageInfo<>(orders);
        List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
        return new PageResult<>(responses, pageInfo.getTotal(), page, size);
    }

    @Override
    public OrderResponse getOrderDetail(Long userId, Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        return OrderResponse.fromEntity(order);
    }

    @Override
    @Transactional
    @SentinelResource(value = "payOrder", blockHandler = "payOrderBlock", fallback = "payOrderFallback")
    public void payOrder(Long userId, Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"待付款".equals(order.getStatus())) {
            throw new BusinessException("订单状态不可支付");
        }
        order.setStatus("待发货");
        orderMapper.update(order);
        rocketMQOrderProducer.sendDelayMessage(order, 24L * 60 * 60 * 1000L, OrderDelayMessage.TYPE_SHIP_TIMEOUT);
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"待付款".equals(order.getStatus())) {
            throw new BusinessException("只能取消待付款订单");
        }
        Goods goods = goodsMapper.findById(order.getGoodsId());
        if (goods != null) {
            goods.setInventory(goods.getInventory() + order.getNum());
            goodsMapper.update(goods);
            stockFeignClient.increaseStock(order.getGoodsId(), order.getNum());
        }
        order.setStatus("已取消");
        orderMapper.update(order);

        // 回补库存后重新计算可售库存，若仍有剩余则重新上架
        syncGoodsStatus(order.getGoodsId());
    }

    @Override
    @Transactional
    public void confirmReceipt(Long userId, Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"待收货".equals(order.getStatus())) {
            throw new BusinessException("订单尚未发货");
        }

        order.setStatus("已完成");
        orderMapper.update(order);

        // 确认收货后没有未完结订单占用了，按"剩余库存"同步上下架：仍有货则保持上架，售罄则置已售
        syncGoodsStatus(order.getGoodsId());

        SignInScore score = new SignInScore();
        score.setOrderNumber(order.getOrderNumber());
        score.setPurchaseUser(userId);
        score.setBusiness(order.getMerchantId());
        score.setPurchaseGoods(order.getTitle());
        score.setCommodityPrice(order.getPrice());
        score.setSignInStatus("已签收");
        signInScoreMapper.insert(score);
    }

    @Override
    @Transactional
    public void rateOrder(Long userId, Long orderId, RateOrderRequest request) {
        Order order = orderMapper.findById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"已完成".equals(order.getStatus())) {
            throw new BusinessException("仅已完成订单可评价");
        }
        order.setStatus("已评价");
        order.setStarRating(request.getStarRating());
        order.setRemarks(request.getRemarks());
        orderMapper.update(order);

        List<SignInScore> scores = signInScoreMapper.findByOrderNumber(order.getOrderNumber());
        if (scores != null && !scores.isEmpty()) {
            SignInScore score = scores.get(0);
            score.setStarRating(request.getStarRating());
            score.setRemarks(request.getRemarks());
            signInScoreMapper.update(score);
        }

        GoodsReview review = goodsReviewMapper.selectByOrderId(orderId);
        if (review == null) {
            review = new GoodsReview();
            review.setOrderId(orderId);
            review.setGoodsId(order.getGoodsId());
            review.setUserId(order.getUserId());
        }
        review.setRating(request.getStarRating());
        review.setContent(request.getRemarks());
        if (review.getReviewId() == null) {
            goodsReviewMapper.insert(review);
        } else {
            goodsReviewMapper.updateByOrderId(review);
        }
    }

    @Override
    public void deleteOrder(Long userId, Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"已完成".equals(order.getStatus()) && !"已取消".equals(order.getStatus())
                && !"已评价".equals(order.getStatus())) {
            throw new BusinessException("只能删除已完成、已评价或已取消的订单");
        }
        orderMapper.deleteById(orderId);
    }

    @Override
    public PageResult<OrderResponse> getSellerOrderList(Long merchantId, String status, String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        List<Order> orders = orderMapper.findByStatus(status, merchantId, keyword, (page - 1) * size, size);
        PageInfo<Order> pageInfo = new PageInfo<>(orders);
        List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
        return new PageResult<>(responses, pageInfo.getTotal(), page, size);
    }

    @Override
    @Transactional
    public void shipOrder(Long merchantId, Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getMerchantId().equals(merchantId)) {
            throw new BusinessException("无权操作该订单");
        }
        if (!"待发货".equals(order.getStatus())) {
            throw new BusinessException("仅待发货订单可发货");
        }
        order.setStatus("待收货");
        orderMapper.update(order);
    }

    public Order createOrderBlock(Long userId, CreateOrderRequest request, BlockException e) {
        throw new BusinessException("下单请求过于频繁，请稍后再试");
    }

    public Order createOrderFallback(Long userId, CreateOrderRequest request, Throwable t) {
        log.error("创建订单失败，触发熔断降级: userId={}", userId, t);
        throw new BusinessException("下单服务暂时不可用，请稍后再试");
    }

    public void payOrderBlock(Long userId, Long orderId, BlockException e) {
        throw new BusinessException("支付请求过于频繁，请稍后再试");
    }

    public void payOrderFallback(Long userId, Long orderId, Throwable t) {
        log.error("支付订单失败，触发熔断降级: orderId={}", orderId, t);
        throw new BusinessException("支付服务暂时不可用，请稍后再试");
    }
}