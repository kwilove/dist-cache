package com.zjhuang.distcache.cache.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.zjhuang.distcache.cache.IAMCache;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 基于Caffeine缓存的抽象
 * <p>
 * Created by hedy on 2023/2/16.
 *
 * @param <T>
 */
public abstract class AbstractCaffeineCache<T> implements IAMCache<Object, T> {

    /**
     * 获取caffeine cache实例
     *
     * @return CaffeineCache
     */
    protected abstract Cache<Object, CacheObjectWrapper<T>> getCache();

    /**
     * 获取有效缓存实例
     * <p>
     * 惰性删除策略，当然如果Cache实例配置了定时策略，可以弥补占用内存的问题
     *
     * @param object 缓存包装对象
     * @return 缓存实例对象
     */
    protected T obtainValidValue(CacheObjectWrapper<T> object) {
        if (Objects.isNull(object)) return null;
        // ttl 0表示永久存活
        if (object.getTtl() > 0) {
            long expireTime = object.getExpireTime();
            long currentTime = System.currentTimeMillis();
            if (currentTime >= expireTime) {
                // 提前释放，降低内存占用
                this.evict(object.getKey());
                return null;
            }
        }
        return object.getValue();
    }

    @Override
    public T getValue(Object key) {
        CacheObjectWrapper<T> object;
        if (this.getCache() instanceof LoadingCache) {
            object = ((LoadingCache<Object, CacheObjectWrapper<T>>) this.getCache()).get(key);
        } else {
            object = this.getCache().getIfPresent(key);
        }
        return this.obtainValidValue(object);
    }

    @Override
    public T getValue(Object key, Function<Object, T> mappingFunction) {
        return this.obtainValidValue(this.getCache().get(key, k -> new CacheObjectWrapper<>(key, mappingFunction.apply(k), 0)));
    }

    @Override
    public void putValue(Object key, T value) {
        putValue(key, value, 0);
    }

    @Override
    public void putValue(Object key, T value, long duration) {
        putValue(key, value, duration, TimeUnit.SECONDS);
    }

    @Override
    public void putValue(Object key, T value, long duration, TimeUnit unit) {
        CacheObjectWrapper<T> object = new CacheObjectWrapper<>(key, value, unit.toMillis(duration));
        getCache().put(key, object);
    }

    @Override
    public void evict(Object key) {
        getCache().invalidate(key);
    }

    @Override
    public void clear() {
        getCache().invalidateAll();
    }
}
