package com.zjhuang.distcache.spring.boot.autoconfigure;

import com.zjhuang.distcache.cache.CacheStorageEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/8/2 6:07 下午
 **/
@Configuration
@ConfigurationProperties(prefix = DistCacheProperties.PREFIX)
public class DistCacheProperties {

    public static final String PREFIX = "dist-cache";

    /**
     * 缓存存储器类型，可选值在{@link CacheStorageEnum}中定义
     */
    private String storage;

    /**
     * 数据分区，用于数据隔离
     */
    private String region;

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
