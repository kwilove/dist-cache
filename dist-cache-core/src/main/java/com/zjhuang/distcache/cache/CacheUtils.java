package com.zjhuang.distcache.cache;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 缓存工具类，提供一些访问缓存静态方法，方便在 converter 中转换用户信息
 *
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/3/29 1:55 下午
 **/
public class CacheUtils implements ApplicationContextAware {

    private static IAMCacheManager cacheManager;

    public static IAMCache getIamCache(String cacheName) {
        return cacheManager.getIAMCache(cacheName);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CacheUtils.cacheManager = applicationContext.getBean(IAMCacheManager.class);
    }
}
