package com.zjhuang.distcache.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 缓存存储器类型
 *
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/5/6 10:27 上午
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheStorageEnum {
    /**
     * 内存
     */
    public static final String MEMORY = "Memory";
    /**
     * Redis存储服务
     */
    public static final String REDIS = "Redis";
    /**
     * 内存+Redis的两级缓存
     */
    public static final String MEMORY_REDIS = "MemoryRedis";
}
