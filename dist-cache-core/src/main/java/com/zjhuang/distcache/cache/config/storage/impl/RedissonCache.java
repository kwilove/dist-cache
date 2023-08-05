package com.zjhuang.distcache.cache.config.storage.impl;

import com.zjhuang.distcache.cache.config.policy.CacheKeyGenerator;
import com.zjhuang.distcache.cache.config.storage.CacheStorage;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存组件
 *
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/10 11:24 上午
 **/
@RequiredArgsConstructor
public class RedissonCache implements CacheStorage<Object, Object>, CacheKeyGenerator {

    private final RedissonClient redissonClient;

    private final Codec codec;

    private final String region;

    /**
     * 缓存key的命名空间，由于redis是个中心化服务，对于不同cacheName的缓存对象，需要通过在key拼接namespace前缀实现隔离，
     * 在CacheAware处设置该值
     *
     * @see com.zjhuang.distcache.cache.config.CacheAware#setApplicationContext(ApplicationContext)
     */
    private String namespace;

    @Override
    public Object getValue(Object key) {
        return redissonClient.getBucket(formatKey(key.toString()), codec).get();
    }

    @Override
    public void putValue(Object key, Object value, long duration, TimeUnit unit) {
        if (duration > 0) {
            redissonClient.getBucket(formatKey(key.toString()), codec).set(value, duration, unit);
        } else {
            redissonClient.getBucket(formatKey(key.toString()), codec).set(value);
        }
    }

    @Override
    public void putValue(Map<Object, Object> elements) {
        elements.forEach((key, value) -> {
            redissonClient.getBucket(formatKey(key.toString()), codec).set(value);
        });
    }

    @Override
    public void evict(Object key) {
        redissonClient.getKeys().delete(formatKey(key.toString()));
    }

    @Override
    public void clear() {
        redissonClient.getKeys().deleteByPattern(formatKey("*"));
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String getRegion() {
        return region;
    }
}
