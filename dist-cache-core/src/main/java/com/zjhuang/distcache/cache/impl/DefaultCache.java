package com.zjhuang.distcache.cache.impl;

import com.zjhuang.distcache.cache.AbstractCache;
import com.zjhuang.distcache.cache.config.storage.CacheStorage;

/**
 * 默认缓存实例
 *
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/12 9:20 下午
 **/
public class DefaultCache extends AbstractCache<Object, Object> {

    private final String cacheName;

    public DefaultCache(String cacheName, CacheStorage cacheStorage) {
        this.cacheName = cacheName;
        this.cacheStorage = cacheStorage;
    }

    @Override
    public String getCacheName() {
        return cacheName;
    }

    @Override
    public String cacheKeyPattern() {
        return "";
    }
}
