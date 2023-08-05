package com.zjhuang.distcache.cache.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.function.Consumer;

/**
 * 本地内存缓存抽象
 * <p>
 * tips:所有业务型实现请放到impl包的子包中 thx!!!
 * <p>
 * Created by hedy on 2023/2/16.
 */
public abstract class IAMMemoryCache<T> extends AbstractCaffeineCache<T> {

    protected final Cache<Object, CacheObjectWrapper<T>> cache;

    protected IAMMemoryCache(Consumer<Caffeine<Object, Object>> cacheConfig) {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder();
        if (cacheConfig != null) cacheConfig.accept(caffeine);
        cache = caffeine.build();
    }

    @Override
    protected Cache<Object, CacheObjectWrapper<T>> getCache() {
        return this.cache;
    }

}
