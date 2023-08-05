package com.zjhuang.distcache.cache.sweeper;

import com.zjhuang.distcache.cache.IAMCache;
import com.zjhuang.distcache.cache.IAMCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 缓存清理工具
 *
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/12 10:50 上午
 */
@Slf4j
public class DefaultCacheSweeper implements CacheSweeper {

    private final RTopic expireTopic;

    private final IAMCacheManager iamCacheManager;

    private final CacheClearMessageSender cacheClearMessageSender;

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private static final String WILDCARD = "*";

    public DefaultCacheSweeper(IAMCacheManager iamCacheManager, RedissonClient redissonClient, CacheClearMessageSender cacheClearMessageSender) {
        this.iamCacheManager = iamCacheManager;
        this.expireTopic = redissonClient.getTopic(CacheClearMessageSender.EXPIRE_TOPIC);
        this.cacheClearMessageSender = cacheClearMessageSender;
    }

    @PostConstruct
    public void init() {
        // 增强一下，通过定时任务实现异步并保证最终能够订阅成功
        SCHEDULED_EXECUTOR.schedule(this::doSubscribeTopic, 1, TimeUnit.MINUTES);
    }

    private void doSubscribeTopic() {
        try {
            expireTopic.addListener(String.class, (channel, message) -> {
                log.info("Receiver a message [{}] from redis topic [{}] to clear cache.", message, CacheClearMessageSender.EXPIRE_TOPIC);
                if (null == message || message.length() == 0) {
                    return;
                }
                String[] array = decodeMessage(message);
                if (array[0].equals(CacheClearMessageSender.DEVICE_ID)) {
                    return;
                }
                String cacheName = array[1];
                doClearLocalCache(cacheName);
            });
            log.info("Success to subscribe topic [{}]", CacheClearMessageSender.EXPIRE_TOPIC);

            // 关闭定时器
            if (!SCHEDULED_EXECUTOR.isShutdown()) {
                SCHEDULED_EXECUTOR.shutdown();
                log.info("Shutdown the subscribe topic scheduledExecutor.");
            }
        } catch (Throwable throwable) {
            log.error("Subscribe topic " + CacheClearMessageSender.EXPIRE_TOPIC + " failed.", throwable);
        }
    }

    private String encodeMessage(String message) {
        return String.format("fromDeviceId:%s,cacheName:%s", CacheClearMessageSender.DEVICE_ID, message);
    }

    private String[] decodeMessage(String message) {
        String[] split = message.split(",");
        String[] res = new String[split.length];
        for (int i = 0; i < split.length; i++) {
            res[i] = split[i].split(":")[1];
        }
        return res;
    }

    @Override
    public void clearAll() {
        try {
            final String message = WILDCARD;
            // 主动清理一次本地缓存
            log.info("Clear all local runtime cache");
            doClearLocalCache(message);
            log.info("Publish a message [{}] to redis topic [{}]", message, CacheClearMessageSender.EXPIRE_TOPIC);
            expireTopic.publish(encodeMessage(message));
        } catch (Throwable throwable) {
            log.error("CacheSweeper.cleanAll error", throwable);
        }
    }

    @Override
    public void clear(String... cacheNames) {
        try {
            if (null == cacheNames || cacheNames.length == 0) {
                return;
            }
            for (String cacheName : cacheNames) {
                // 主动清理一次本地缓存
                log.info("Clear local runtime cache, cacheName [{}]", cacheName);
                doClearLocalCache(cacheName);
                cacheClearMessageSender.clear(getIAMCache(cacheName));
            }
        } catch (Throwable throwable) {
            log.error("CacheSweeper.clear error, cacheNames [{}]", Arrays.toString(cacheNames), throwable);
        }
    }

    @Override
    public void evict(String cacheName, Object cacheKey) {
        IAMCache cache = getIAMCache(cacheName);
        cacheClearMessageSender.evict(cache, cacheKey);
    }

    private void doClearLocalCache(String key) {
        iamCacheManager.getCacheNames().forEach(cacheName -> {
            if (WILDCARD.equals(key) || cacheName.equals(key)) {
                IAMCache iamCache = getIAMCache(cacheName);
                log.info("Cache instance [{}] named [{}] will be cleared", iamCache.getClass().getName(), cacheName);
                iamCache.getCacheStorage().clear();
            }
        });
    }

    public IAMCache getIAMCache(String name) {
        return iamCacheManager.getIAMCache(name);
    }
}
