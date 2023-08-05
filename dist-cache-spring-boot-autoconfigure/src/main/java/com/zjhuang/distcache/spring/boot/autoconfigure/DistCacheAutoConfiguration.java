package com.zjhuang.distcache.spring.boot.autoconfigure;

import com.zjhuang.distcache.cache.CacheStorageEnum;
import com.zjhuang.distcache.cache.CacheUtils;
import com.zjhuang.distcache.cache.IAMCache;
import com.zjhuang.distcache.cache.IAMCacheManager;
import com.zjhuang.distcache.cache.config.CacheAware;
import com.zjhuang.distcache.cache.config.CacheInjectSuggest;
import com.zjhuang.distcache.cache.config.proxy.CacheProxyFactory;
import com.zjhuang.distcache.cache.config.storage.impl.CaffeineCache;
import com.zjhuang.distcache.cache.config.storage.impl.MemoryRedisCache;
import com.zjhuang.distcache.cache.config.storage.impl.RedissonCache;
import com.zjhuang.distcache.cache.sweeper.CacheClearMessageSender;
import com.zjhuang.distcache.cache.sweeper.CacheSweeper;
import com.zjhuang.distcache.cache.sweeper.DefaultCacheSweeper;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import java.util.List;

/**
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/12 11:14 上午
 **/
@EnableCaching
public class DistCacheAutoConfiguration {

    @Bean
    public IAMCacheManager cacheManager(List<IAMCache> caches) {
        return new IAMCacheManager(caches);
    }

    @Bean
    @ConditionalOnBean(IAMCacheManager.class)
    public CacheUtils cacheUtils() {
        return new CacheUtils();
    }

    /**
     * 配置 scope = prototype，可以保证每个 AbstractCache 实现类对象都分配独立的 CaffeineCache 对象
     */
    @Bean
    @Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
    @ConditionalOnProperty(prefix = "iam.cache", name = "storage", havingValue = CacheStorageEnum.MEMORY, matchIfMissing = true)
    public CaffeineCache caffeineCache() {
        return new CaffeineCache();
    }

    @Bean
    public Codec codec() {
        return new JsonJacksonCodec();
    }

    @Bean
    @Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
    @ConditionalOnProperty(prefix = "iam.cache", name = "storage", havingValue = CacheStorageEnum.REDIS)
    public RedissonCache redissonCache(RedissonClient redissonClient, Codec codec,
                                       @Value("${iam.cache.region}") String region) {
        return new RedissonCache(redissonClient, codec, region);
    }

    @Bean
    @Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
    @ConditionalOnProperty(prefix = "iam.cache", name = "storage", havingValue = CacheStorageEnum.MEMORY_REDIS)
    public MemoryRedisCache memoryRedisCache(RedissonClient redissonClient, Codec codec,
                                             @Value("${iam.cache.region}") String region) {
        RedissonCache redissonCache = new RedissonCache(redissonClient, codec, region);
        return new MemoryRedisCache(redissonCache);
    }

    @Bean
    public CacheAware cacheAware() {
        return new CacheAware();
    }

    @Bean
    public CacheClearMessageSender cacheClearMessageSender(@Lazy RedissonClient redissonClient) {
        return new CacheClearMessageSender(redissonClient);
    }

    @Bean
    public CacheProxyFactory cacheProxyFactory(ConfigurableListableBeanFactory configurableListableBeanFactory,
                                               CacheClearMessageSender cacheClearMessageSender) {
        return new CacheProxyFactory(configurableListableBeanFactory, cacheClearMessageSender);
    }

    @Bean
    @ConditionalOnBean({CaffeineCache.class, MemoryRedisCache.class})
    public CacheInjectSuggest cacheInjectSuggest(CacheProxyFactory cacheProxyFactory) {
        return new CacheInjectSuggest(cacheProxyFactory);
    }

    @Bean
    public CacheSweeper cacheSweeper(IAMCacheManager iamCacheManager,
                                     RedissonClient redissonClient,
                                     CacheClearMessageSender cacheClearMessageSender) {
        return new DefaultCacheSweeper(iamCacheManager, redissonClient, cacheClearMessageSender);
    }
}
