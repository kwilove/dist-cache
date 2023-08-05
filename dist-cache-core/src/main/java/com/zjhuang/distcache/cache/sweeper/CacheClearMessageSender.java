package com.zjhuang.distcache.cache.sweeper;

import com.zjhuang.distcache.cache.IAMCache;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

import java.text.MessageFormat;
import java.util.UUID;

/**
 * 缓存清理消息发送器
 *
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/14 9:44 上午
 **/
@Slf4j
public class CacheClearMessageSender {

    /**
     * 设备ID
     */
    public static final String DEVICE_ID = UUID.randomUUID().toString();
    /**
     * 缓存失效主题
     */
    public static final String EXPIRE_TOPIC = "IAMCacheSweeper";

    private final RedissonClient redissonClient;

    public CacheClearMessageSender(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void evict(IAMCache iamCache, Object cacheKey) {
        String cacheName = iamCache.getCacheName();
        try {
            iamCache.getCacheStorage().evict(cacheKey);
            log.debug("Cache instance [{}] named [{}] evicted cache key [{}]",
                iamCache.getClass().getName(), iamCache.getCacheName(), cacheKey);
            redissonClient.getTopic(EXPIRE_TOPIC).publish(encodeMessage(iamCache.getCacheName()));
        } catch (Throwable throwable) {
            log.error(MessageFormat.format("AbstractCacheSweeper.evict error, cacheName [{0}]", cacheName), throwable);
        }
    }

    public void clear(IAMCache iamCache) {
        String cacheName = iamCache.getCacheName();
        try {
            if (null == cacheName || cacheName.length() == 0) {
                return;
            }
            log.debug("publish a message [{}] to redis topic [{}]", cacheName, EXPIRE_TOPIC);
            redissonClient.getTopic(EXPIRE_TOPIC).publish(encodeMessage(cacheName));
        } catch (Throwable throwable) {
            log.error(MessageFormat.format("AbstractCacheSweeper.clear error, cacheName [{0}]", cacheName), throwable);
        }
    }

    private String encodeMessage(String message) {
        return String.format("fromDeviceId:%s,cacheName:%s", DEVICE_ID, message);
    }
}
