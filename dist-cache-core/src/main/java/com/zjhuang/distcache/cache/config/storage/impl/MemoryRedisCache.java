package com.zjhuang.distcache.cache.config.storage.impl;

import com.zjhuang.distcache.cache.config.policy.CacheKeyGenerator;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/11 2:43 下午
 **/
@RequiredArgsConstructor
public class MemoryRedisCache extends CaffeineCache implements CacheKeyGenerator {

    private final RedissonCache redissonCache;

    @Override
    public Object getValue(Object key) {
        Object value = super.getValue(key);
        if (Objects.isNull(value)) {
            return redissonCache.getValue(key);
        }
        return value;
    }

    @Override
    public void putValue(Object key, Object value, long duration, TimeUnit unit) {
        super.putValue(key, value, duration, unit);
        redissonCache.putValue(key, value, duration, unit);
    }

    @Override
    public void putValue(Map<Object, Object> elements) {
        super.putValue(elements);
        redissonCache.putValue(elements);
    }

    @Override
    public void evict(Object key) {
        super.evict(key);
        redissonCache.evict(key);
    }

    @Override
    public void clear() {
        super.clear();
        redissonCache.clear();
    }

    @Override
    public void setNamespace(String namespace) {
        this.redissonCache.setNamespace(namespace);
    }
}
