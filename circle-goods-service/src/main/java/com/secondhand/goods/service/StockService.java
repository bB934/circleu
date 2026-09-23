package com.secondhand.goods.service;

import com.secondhand.common.BusinessException;
import com.secondhand.entity.Goods;
import com.secondhand.mapper.GoodsMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class StockService {

    private static final String STOCK_KEY_PREFIX = "stock:goods:";

    private static final DefaultRedisScript<Long> DECREASE_SCRIPT;

    static {
        DECREASE_SCRIPT = new DefaultRedisScript<>();
        DECREASE_SCRIPT.setLocation(new org.springframework.core.io.ClassPathResource("lua/decrease_stock.lua"));
        DECREASE_SCRIPT.setResultType(Long.class);
    }

    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final GoodsMapper goodsMapper;

    public StockService(StringRedisTemplate redisTemplate, RedissonClient redissonClient, GoodsMapper goodsMapper) {
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
        this.goodsMapper = goodsMapper;
    }

    /**
     * 扣减库存，返回扣减后的剩余库存。
     * 剩余库存用于调用方判断商品是否需要立即下架（售罄），避免出现"上架但库存为0"。
     */
    public int decreaseStock(Long goodsId, Integer num) {
        String stockKey = STOCK_KEY_PREFIX + goodsId;
        try {
            Long result = redisTemplate.execute(DECREASE_SCRIPT, List.of(stockKey), String.valueOf(num));
            if (result == null) {
                throw new BusinessException("库存服务异常");
            }
            if (result == -1L) {
                throw new BusinessException("库存不足");
            }
            log.info("Redis+Lua 原子扣减库存成功: goodsId={}, num={}, remain={}", goodsId, num, result);
            return result.intValue();
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            }
            log.warn("Redis+Lua 不可用，降级为 Redisson 分布式锁 + DB: goodsId={}", goodsId, e);
            return decreaseStockWithLock(goodsId, num);
        }
    }

    public void increaseStock(Long goodsId, Integer num) {
        String stockKey = STOCK_KEY_PREFIX + goodsId;
        try {
            Long remain = redisTemplate.opsForValue().increment(stockKey, num);
            log.info("Redis回补库存: goodsId={}, num={}, remain={}", goodsId, num, remain);
        } catch (Exception e) {
            log.warn("Redis回补库存失败，降级为直接DB: goodsId={}", goodsId, e);
        }
    }

    public void syncStockToRedis(Long goodsId) {
        Goods goods = goodsMapper.findById(goodsId);
        if (goods != null && goods.getInventory() != null) {
            String stockKey = STOCK_KEY_PREFIX + goodsId;
            redisTemplate.opsForValue().set(stockKey, String.valueOf(goods.getInventory()));
        }
    }

    private int decreaseStockWithLock(Long goodsId, Integer num) {
        String lockKey = "stock:lock:" + goodsId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }
            try {
                Goods goods = goodsMapper.findById(goodsId);
                if (goods == null || goods.getStatus() != 1) {
                    throw new BusinessException("商品已下架或已售出");
                }
                if (goods.getInventory() < num) {
                    throw new BusinessException("库存不足: " + goods.getTitle());
                }
                goods.setInventory(goods.getInventory() - num);
                goodsMapper.update(goods);
                return goods.getInventory();
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统繁忙，请稍后重试");
        }
    }
}