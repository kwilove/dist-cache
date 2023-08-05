package com.zjhuang.distcache.cache;

import com.zjhuang.distcache.cache.config.storage.CacheStorage;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @param <K>
 * @param <V>
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/11 3:21 下午
 **/
public abstract class AbstractCache<K, V> implements IAMCache<K, V> {

    protected CacheStorage<K, V> cacheStorage;

    @Override
    public CacheStorage getCacheStorage() {
        return cacheStorage;
    }

    @Override
    public V getValue(K key) {
        return cacheStorage.getValue(key);
    }

    @Override
    public V getValue(K key, Function<K, V> mappingFunction) {
        V value = cacheStorage.getValue(key);
        if (Objects.isNull(value)) {
            value = mappingFunction.apply(key);
        }
        return value;
    }

    @Override
    public void putValue(K key, V value, long duration, TimeUnit unit) {
        cacheStorage.putValue(key, value, duration, unit);
    }

    @Override
    public void evict(K key) {
        cacheStorage.evict(key);
    }

    @Override
    public void clear() {
        cacheStorage.clear();
    }

    public void initCache(CacheStorage<K, V> cacheStorage) {
        this.cacheStorage = cacheStorage;
    }
}
