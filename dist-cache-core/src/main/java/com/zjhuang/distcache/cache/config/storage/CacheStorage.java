package com.zjhuang.distcache.cache.config.storage;

import com.zjhuang.distcache.cache.ICache;

/**
 * 缓存存储器
 * <p>
 * 可选值在{@link com.zjhuang.distcache.cache.CacheStorageEnum}中定义
 *
 * @param <K>
 * @param <V>
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/11 4:21 下午
 **/
public interface CacheStorage<K, V> extends ICache<K, V> {
}
