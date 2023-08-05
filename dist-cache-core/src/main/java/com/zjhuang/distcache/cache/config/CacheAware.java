package com.zjhuang.distcache.cache.config;

import com.zjhuang.distcache.cache.AbstractCache;
import com.zjhuang.distcache.cache.config.policy.CacheKeyGenerator;
import com.zjhuang.distcache.cache.config.storage.CacheStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/11 4:05 下午
 **/
@Slf4j
@RequiredArgsConstructor
public class CacheAware implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        applicationContext.getBeansOfType(AbstractCache.class).forEach((beanName, abstractCache) -> {
            // 获取getBean获取原型CacheStorage对象注入进AbstractCache对象
            CacheStorage cacheStorage = applicationContext.getBean(CacheStorage.class);
            log.info("Target bean: {}, cacheStorage: {}", beanName, cacheStorage);
            if (CacheKeyGenerator.class.isAssignableFrom(cacheStorage.getClass())) {
                String cacheName = abstractCache.getCacheName();
                ((CacheKeyGenerator) cacheStorage).setNamespace(cacheName);
                log.info("CacheKeyGenerator.setNamespace to {}", cacheName);
            }
            abstractCache.initCache(cacheStorage);
        });
    }
}
