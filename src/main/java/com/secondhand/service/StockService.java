package com.secondhand.service;

import com.secondhand.common.BusinessException;
import com.secondhand.entity.Goods;
import com.secondhand.mapper.GoodsMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class StockService {

    private static final String STOCK_KEY = "goods:stock:";
    private static final String STATUS_KEY = "goods:status:";

    private final RedissonClient redissonClient;
    private final GoodsMapper goodsMapper;
    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> deductScript;
    private final DefaultRedisScript<Long> restoreScript;

    public StockService(RedissonClient redissonClient, GoodsMapper goodsMapper,
                        StringRedisTemplate redisTemplate) {
        this.redissonClient = redissonClient;
        this.goodsMapper = goodsMapper;
        this.redisTemplate = redisTemplate;
        this.deductScript = new DefaultRedisScript<>();
        this.deductScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/stock_deduct.lua")));
        this.deductScript.setResultType(Long.class);
        this.restoreScript = new DefaultRedisScript<>();
        this.restoreScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/stock_restore.lua")));
        this.restoreScript.setResultType(Long.class);
    }

    /**
     * 扣减库存：优先用 Redis + Lua 脚本原子预扣（高性能、防超卖），
     * 再用 Redisson 分布式锁保证 DB 与 Redis 库存最终一致（回写/对账）。
     * Redis 不可用时降级为直接 DB 校验扣减，保证下单主流程可用。
     */
    public void decreaseStock(Long goodsId, Integer num) {
        String lockKey = "stock:lock:" + goodsId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new BusinessException("系统繁忙，请稍后重试");
            }
            try {
                if (!deductByLua(goodsId, num)) {
                    throw new BusinessException("库存不足或商品已下架");
                }
                // 持有锁后回写 DB，确保 Redis 与 MySQL 库存一致
                Goods goods = goodsMapper.findById(goodsId);
                if (goods == null || goods.getStatus() != 1
                        || goods.getInventory() < num) {
                    restoreByLua(goodsId, num);
                    throw new BusinessException("库存不足或商品已下架");
                }
                goods.setInventory(goods.getInventory() - num);
                goodsMapper.update(goods);
                log.info("Lua预扣+DB回写库存成功: goodsId={}, num={}, remain={}",
                        goodsId, num, goods.getInventory());
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统繁忙，请稍后重试");
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            }
            // Redis/Redisson 不可用时降级：直接 DB 校验扣减
            log.warn("Redis预扣不可用，降级为直接库存扣减: goodsId={}", goodsId, e);
            Goods goods = goodsMapper.findById(goodsId);
            if (goods == null || goods.getStatus() != 1) {
                throw new BusinessException("商品已下架或已售出");
            }
            if (goods.getInventory() < num) {
                throw new BusinessException("库存不足: " + goods.getTitle());
            }
            goods.setInventory(goods.getInventory() - num);
            goodsMapper.update(goods);
        }
    }

    /**
     * 回补库存（取消/超时关单时调用）：Redis 原子回补 + DB 回补。
     */
    public void restoreStock(Long goodsId, Integer num) {
        try {
            restoreByLua(goodsId, num);
        } catch (Exception e) {
            log.warn("Redis回补库存失败，仅回补DB: goodsId={}", goodsId, e);
        }
        Goods goods = goodsMapper.findById(goodsId);
        if (goods != null) {
            goods.setInventory(goods.getInventory() + num);
            goodsMapper.update(goods);
        }
    }

    /**
     * 发布商品时预热 Redis 库存与状态，提升抢购命中率。
     */
    public void warmUp(Long goodsId, Integer inventory, Integer status) {
        try {
            redisTemplate.opsForValue().set(STOCK_KEY + goodsId, String.valueOf(inventory));
            redisTemplate.opsForValue().set(STATUS_KEY + goodsId, String.valueOf(status));
        } catch (Exception e) {
            log.warn("预热Redis库存失败: goodsId={}", goodsId, e);
        }
    }

    private boolean deductByLua(Long goodsId, Integer num) {
        Long result = redisTemplate.execute(deductScript,
                Collections.singletonList(STOCK_KEY + goodsId),
                String.valueOf(num), STATUS_KEY + goodsId);
        return result != null && result == 1L;
    }

    private void restoreByLua(Long goodsId, Integer num) {
        redisTemplate.execute(restoreScript,
                Collections.singletonList(STOCK_KEY + goodsId),
                String.valueOf(num));
    }
}
