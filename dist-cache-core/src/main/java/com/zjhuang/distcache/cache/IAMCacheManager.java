package com.zjhuang.distcache.cache;

import com.zjhuang.distcache.cache.config.storage.CacheStorage;
import com.zjhuang.distcache.cache.impl.DefaultCache;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 实现Spring CacheManager，注入到Spring体系
 * <p>
 * 使用SpringCache机制管理整个IAM的缓存组件
 * <p>
 * Created by hedy on 2023/2/16.
 */
@SuppressWarnings("all")
public class IAMCacheManager implements CacheManager, BeanFactoryAware {

    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<>(16);

    private final Set<String> cacheNames = new LinkedHashSet<>(16);

    private BeanFactory beanFactory;

    public IAMCacheManager(List<IAMCache> caches) {
        caches.forEach(cache -> {
            this.caches.put(cache.getCacheName(), new SpringCacheWrapper(cache));
            this.cacheNames.add(cache.getCacheName());
        });
    }

    public <T extends IAMCache> T getIAMCache(String name) {
        return (T) this.caches.get(name).getNativeCache();
    }

    @Override
    public Cache getCache(String name) {
        Cache cache = this.caches.get(name);
        if (cache == null) {
            this.caches.computeIfAbsent(name,
                key -> new SpringCacheWrapper(
                    new DefaultCache(key,
                        beanFactory.getBean(CacheStorage.class))));
            synchronized (this.cacheNames) {
                this.cacheNames.add(name);
            }
        }

        return this.caches.get(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        synchronized (this.cacheNames) {
            return Collections.unmodifiableSet(this.cacheNames);
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
