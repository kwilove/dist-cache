package com.zjhuang.distcache.cache.sweeper;

/**
 * 缓存清理工具
 *
 * @author hzj273812（黄子敬）
 * @email hzj273812@alibaba-inc.com
 * @create 2022/7/7 8:51 下午
 **/
public interface CacheSweeper {

    void clearAll();

    void clear(String... cacheNames);

    void evict(String cacheName, Object cacheKey);
}
