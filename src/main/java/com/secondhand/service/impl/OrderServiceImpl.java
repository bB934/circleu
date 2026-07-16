package com.secondhand.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.secondhand.common.BusinessException;
import com.secondhand.common.PageResult;
import com.secondhand.dto.request.CreateOrderRequest;
import com.secondhand.dto.request.RateOrderRequest;
import com.secondhand.dto.response.OrderResponse;
import com.secondhand.entity.*;
import com.secondhand.mapper.*;
import com.secondhand.mq.OrderDelayProducer;
import com.secondhand.mq.OrderDelayMessage;
import com.secondhand.service.OrderService;
import com.secondhand.service.StockService;
import com.secondhand.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final CartMapper cartMapper;
    private final GoodsMapper goodsMapper;
    private final AddressMapper addressMapper;
    private final SignInScoreMapper signInScoreMapper;
    private final StockService stockService;
    private final OrderDelayProducer orderDelayProducer;
    private final GoodsReviewMapper goodsReviewMapper;
    private final IdGenerator idGenerator;

    @Override
    @Transactional
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

        Order firstOrder = null;
        for (Cart cart : cartList) {
            Goods goods = goodsMapper.findById(cart.getGoodsId());
            if (goods == null || goods.getStatus() != 1) {
                throw new BusinessException("商品已下架或已售出: " + cart.getGoodsId());
            }

            // 分布式锁扣减库存（防超卖）
            stockService.decreaseStock(goods.getSecondHandMallId(), cart.getNum());

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
            order.setRated(0);
            orderMapper.insert(order);

            cartMapper.deleteById(cart.getCartId());

            // 发送订单超时自动取消延迟消息（15 分钟）
            orderDelayProducer.sendDelayMessage(order, 15 * 60 * 1000L);

            if (firstOrder == null) {
                firstOrder = order;
            }
        }
        return firstOrder;
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
        // 支付成功进入待发货：若卖家 24h 内未发货，自动取消并回补库存
        orderDelayProducer.sendDelayMessage(order, 24L * 60 * 60 * 1000L, OrderDelayMessage.TYPE_SHIP_TIMEOUT);
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
        stockService.restoreStock(order.getGoodsId(), order.getNum());
        order.setStatus("已取消");
        orderMapper.update(order);
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

        // 库存已在下单时扣减；仅当剩余库存为 0 才置已售(隐藏)，否则保持在上售状态
        Goods goods = goodsMapper.findById(order.getGoodsId());
        int remain = (goods != null && goods.getInventory() != null) ? goods.getInventory() : 0;
        goodsMapper.updateStatus(order.getGoodsId(), remain <= 0 ? 2 : 1);

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
        if (order.getRated() != null && order.getRated() == 1) {
            throw new BusinessException("订单已评价，不能重复评价");
        }
        order.setStatus("已评价");
        order.setStarRating(request.getStarRating());
        order.setRemarks(request.getRemarks());
        order.setRated(1);
        orderMapper.update(order);

        List<SignInScore> scores = signInScoreMapper.findByOrderNumber(order.getOrderNumber());
        if (scores != null && !scores.isEmpty()) {
            SignInScore score = scores.get(0);
            score.setStarRating(request.getStarRating());
            score.setRemarks(request.getRemarks());
            signInScoreMapper.update(score);
        }

        // 双写商品评价到独立表 goods_review（按 order_id upsert）
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
}
