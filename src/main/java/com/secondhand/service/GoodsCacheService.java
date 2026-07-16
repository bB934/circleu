package com.secondhand.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.secondhand.common.BusinessException;
import com.secondhand.entity.Goods;
import com.secondhand.mapper.GoodsMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商品缓存防穿透组件：
 * 1) 布隆过滤器：拦截不存在的商品 ID（缓存穿透），避免恶意/异常 ID 击穿 DB
 * 2) 多级缓存：本地 Caffeine（L1） -> Redis（L2） -> MySQL（L3）
 * 3) 空值缓存：对确实不存在的 key 缓存短过期空标记，进一步拦截重复穿透
 */
@Slf4j
@Service
public class GoodsCacheService {

    private static final String BLOOM_NAME = "goods:id:bloom";
    private static final String NULL_KEY = "goods:null:";
    private static final long NULL_TTL_SECONDS = 60;
    private static final long L2_TTL_SECONDS = 1800;

    private final RedissonClient redissonClient;
    private final GoodsMapper goodsMapper;
    private final StringRedisTemplate redisTemplate;

    // L1 本地缓存：最多 1万条，写后 5 分钟过期
    private final Cache<Long, Goods> localCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    private RBloomFilter<Long> bloomFilter;

    public GoodsCacheService(RedissonClient redissonClient, GoodsMapper goodsMapper,
                             StringRedisTemplate redisTemplate) {
        this.redissonClient = redissonClient;
        this.goodsMapper = goodsMapper;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void initBloom() {
        try {
            bloomFilter = redissonClient.getBloomFilter(BLOOM_NAME);
            if (!bloomFilter.tryInit(100_000L, 0.01)) {
                // 已存在则复用
                bloomFilter = redissonClient.getBloomFilter(BLOOM_NAME);
            }
            // 启动时把已上架商品ID载入布隆过滤器，避免存量数据被误判为不存在
            List<Long> existingIds = goodsMapper.selectAllOnSaleIds();
            for (Long id : existingIds) {
                bloomFilter.add(id);
            }
            log.info("布隆过滤器初始化完成，载入存量商品 {} 条", existingIds.size());
        } catch (Exception e) {
            log.warn("布隆过滤器初始化失败，降级为直接查询: {}", e.getMessage());
        }
    }

    /**
     * 多级缓存查询商品：布隆过滤 -> L1 -> L2 -> DB，命中后逐级回写。
     * 返回 null 表示商品不存在（已做空值缓存）。
     */
    public Goods getGoods(Long goodsId) {
        if (!mightExist(goodsId)) {
            // 布隆过滤器判定不存在，直接拦截，保护 DB
            return null;
        }
        // L1 本地缓存
        Goods g = localCache.getIfPresent(goodsId);
        if (g != null) {
            return g;
        }
        // L2 Redis 空值标记
        if (Boolean.TRUE.equals(redisTemplate.hasKey(NULL_KEY + goodsId))) {
            return null;
        }
        // L3 DB
        g = goodsMapper.findById(goodsId);
        if (g == null) {
            redisTemplate.opsForValue().set(NULL_KEY + goodsId, "1",
                    NULL_TTL_SECONDS, TimeUnit.SECONDS);
            return null;
        }
        localCache.put(goodsId, g);
        redisTemplate.opsForValue().set("goods:detail:" + goodsId,
                String.valueOf(g.getInventory()), L2_TTL_SECONDS, TimeUnit.SECONDS);
        return g;
    }

    /**
     * 商品发布/上架时加入布隆过滤器并预热 L1/L2。
     */
    public void addGoods(Long goodsId) {
        try {
            if (bloomFilter != null) {
                bloomFilter.add(goodsId);
            }
        } catch (Exception e) {
            log.warn("布隆过滤器写入失败: goodsId={}", goodsId, e);
        }
        localCache.invalidate(goodsId);
        try {
            redisTemplate.delete(NULL_KEY + goodsId);
        } catch (Exception ignored) {
        }
    }

    /**
     * 商品下架/删除时失效各级缓存。
     */
    public void removeGoods(Long goodsId) {
        localCache.invalidate(goodsId);
        try {
            redisTemplate.delete(NULL_KEY + goodsId);
            redisTemplate.delete("goods:detail:" + goodsId);
        } catch (Exception ignored) {
        }
    }

    public void refreshStock(Long goodsId, Integer inventory) {
        localCache.invalidate(goodsId);
        try {
            redisTemplate.opsForValue().set("goods:detail:" + goodsId,
                    String.valueOf(inventory), L2_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
    }

    private boolean mightExist(Long goodsId) {
        try {
            return bloomFilter == null || bloomFilter.contains(goodsId);
        } catch (Exception e) {
            return true; // 布隆不可用则放行到 DB
        }
    }
}
