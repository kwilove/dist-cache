package com.zjhuang.distcache.cache;

import com.zjhuang.distcache.cache.config.policy.CacheNullValuePolicy;

import java.util.Objects;

/**
 * 支持懒加载的缓存组件
 * <p>
 * 当缓存查询结果为null时，将尝试调用{@link AbstractLoadingCache#cacheLoader(K)}加载目标数据，并将结果进行缓存
 *
 * @param <K>
 * @param <V>
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/11 3:19 下午
 **/
public abstract class AbstractLoadingCache<K, V> extends AbstractCache<K, V> implements CacheNullValuePolicy {

    protected abstract V cacheLoader(K key);

    @Override
    public V getValue(K key) {
        V value = super.getValue(key);
        if (Objects.isNull(value)) {
            value = cacheLoader(key);
            if (Objects.isNull(value) && !cacheNullValue()) {
                return null;
            }
            super.putValue(key, value, timeToLive(), timeUnit());
        }
        return value;
    }
}
