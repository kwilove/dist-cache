package com.zjhuang.distcache.cache.config.policy;

/**
 * 空值缓存策略
 *
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/12 10:50 上午
 **/
public interface CacheNullValuePolicy {

    /**
     * @return 当目标数据为null时是否缓存
     */
    default boolean cacheNullValue() {
        return false;
    }
}
