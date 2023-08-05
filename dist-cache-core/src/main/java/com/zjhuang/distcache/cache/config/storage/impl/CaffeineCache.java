package com.zjhuang.distcache.cache.config.storage.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zjhuang.distcache.cache.IAMCache;
import com.zjhuang.distcache.cache.config.storage.CacheStorage;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/11 2:22 下午
 **/
@Slf4j
public class CaffeineCache implements CacheStorage<Object, Object> {

    private final Cache<Object, IAMCache.CacheObjectWrapper<Object>> cache;

    // 存储非永久的缓存对象，最小堆
    private final PriorityQueue<IAMCache.CacheObjectWrapper<Object>> impermanentCacheObject
        = new PriorityQueue<>(Comparator.comparingLong(IAMCache.CacheObjectWrapper::getExpireTime));

    // 定时任务线程池
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    // 每5s执行一次定时任务
    private static final long DELAY = 5;
    // 每次处理500个
    private static final long ITERATION = 500;

    public CaffeineCache() {
        cache = Caffeine.newBuilder().build();
        scheduler.scheduleWithFixedDelay(() -> {
            log.debug("start scheduler that evict expire keys");
            for (long i = 0; i < ITERATION; i++) {
                if (impermanentCacheObject.isEmpty()) {
                    log.debug("impermanentCacheObject is empty, end of current schedule task");
                    return;
                }
                IAMCache.CacheObjectWrapper<Object> object = impermanentCacheObject.peek();
                // ttl 0表示永久存活
                if (object.getTtl() > 0) {
                    long expireTime = object.getExpireTime();
                    long currentTime = System.currentTimeMillis();
                    if (currentTime >= expireTime) {
                        // 提前释放，降低内存占用
                        this.evict(String.valueOf(object.getKey()));
                        log.debug("evict cache key: {}, expireTime: {}", object.getKey(), expireTime);
                    } else {
                        log.debug("no any expire keys, end of current schedule task");
                        // impermanentCacheObject 是个最小堆，按照过期时间戳从小到大排序，如果当前对象未过期，后面的肯定也未过期
                        return;
                    }
                }
            }
        }, DELAY, DELAY, TimeUnit.SECONDS);
    }

    /**
     * 获取有效缓存实例
     * <p>
     * 惰性删除策略，当然如果Cache实例配置了定时策略，可以弥补占用内存的问题
     *
     * @param object 缓存包装对象
     * @return 缓存实例对象
     */
    protected Object obtainValidValue(IAMCache.CacheObjectWrapper<Object> object) {
        if (Objects.isNull(object)) {
            return null;
        }
        // ttl 0表示永久存活
        if (object.getTtl() > 0) {
            long expireTime = object.getExpireTime();
            long currentTime = System.currentTimeMillis();
            if (currentTime >= expireTime) {
                // 提前释放，降低内存占用
                this.evict(String.valueOf(object.getKey()));
                return null;
            }
        }
        return object.getValue();
    }

    @Override
    public Object getValue(Object key) {
        return obtainValidValue(cache.getIfPresent(key));
    }

    @Override
    public void putValue(Object key, Object value, long duration, TimeUnit unit) {
        IAMCache.CacheObjectWrapper<Object> object = new IAMCache.CacheObjectWrapper<>(key, value, unit.toMillis(duration));
        cache.put(key, object);
        if (duration > 0) {
            impermanentCacheObject.add(object);
        }
    }

    @Override
    public void putValue(Map<Object, Object> elements) {
        Map<Object, IAMCache.CacheObjectWrapper<Object>> newElements = new HashMap<>(elements.size());
        elements.forEach((key, value) -> {
            IAMCache.CacheObjectWrapper<Object> object = new IAMCache.CacheObjectWrapper<>(key, value);
            newElements.put(key, object);
        });
        cache.putAll(newElements);
    }

    @Override
    public void evict(Object key) {
        impermanentCacheObject.remove(cache.getIfPresent(key));
        cache.invalidate(key);
    }

    @Override
    public void clear() {
        cache.cleanUp();
        impermanentCacheObject.clear();
    }

    @PreDestroy
    public void preDestroy() {
        log.info("shutdownNow scheduler that evict expire keys");
        scheduler.shutdownNow();
    }
}
