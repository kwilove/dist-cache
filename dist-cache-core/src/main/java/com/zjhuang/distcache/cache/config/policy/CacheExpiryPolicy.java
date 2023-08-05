package com.zjhuang.distcache.cache.config.policy;

import java.util.concurrent.TimeUnit;

/**
 * 过期策略
 *
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/11 5:21 下午
 **/
public interface CacheExpiryPolicy {

    /**
     * 表示永久有效的TTL
     */
    long PERMANENT_TIME_TO_LIVE = 0;

    /**
     * @return 有效时长
     * <p>
     * {@link CacheExpiryPolicy#PERMANENT_TIME_TO_LIVE} 代表永久有效
     */
    default long timeToLive() {
        return PERMANENT_TIME_TO_LIVE;
    }

    /**
     * @return 有效时间单位
     */
    default TimeUnit timeUnit() {
        return TimeUnit.SECONDS;
    }
}
