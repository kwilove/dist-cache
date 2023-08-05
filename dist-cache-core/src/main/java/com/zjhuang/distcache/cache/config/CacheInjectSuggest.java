package com.zjhuang.distcache.cache.config;

import com.zjhuang.distcache.cache.AbstractCache;
import com.zjhuang.distcache.cache.config.proxy.CacheProxyFactory;
import com.zjhuang.distcache.proxy.InjectSuggest;
import lombok.RequiredArgsConstructor;

/**
 * 缓存对象的注册推荐策略
 *
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/14 1:42 下午
 **/
@RequiredArgsConstructor
public class CacheInjectSuggest implements InjectSuggest {

    private final CacheProxyFactory cacheProxyFactory;

    @Override
    public boolean support(Class<?> type) {
        return AbstractCache.class.isAssignableFrom(type);
    }

    @Override
    public Object getValue(Class<?> type) {
        return cacheProxyFactory.getCacheProxy(type);
    }
}
