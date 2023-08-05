package com.zjhuang.distcache.cache;

import com.zjhuang.distcache.cache.config.policy.CacheExpiryPolicy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存数据操作接口，声明一些针对缓存的原子性操作
 * <p>
 * API 应该尽量遵守 JSR 107 Cache 规范
 * <p>
 * PS: 从产品成熟度、功能完善程度、项目案例验证等方面考虑，多级缓存都应该优先引入主流的多级缓存框架，比如J2Cache
 *
 * @param <K> cache key type
 * @param <V> cache value type
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/10 5:15 下午
 **/
public interface ICache<K, V> extends CacheExpiryPolicy {

    /**
     * 从缓存中获得对象，当对象不在缓存中或已经过期返回null
     *
     * @param key 缓存标识
     * @return 缓存实例对象
     */
    V getValue(K key);

    /**
     * 批量获取缓存数据
     *
     * @param keys 批量key
     * @return key mapped to the cached object or null
     */
    default Map<K, V> getAllValue(Set<K> keys) {
        HashMap<K, V> valueMap = new HashMap<>();
        for (K key : keys) {
            valueMap.put(key, getValue(key));
        }
        return valueMap;
    }

    /**
     * 判断缓存是否存在
     *
     * @param key cache key
     * @return true if key exists
     */
    default boolean exists(K key) {
        return getValue(key) != null;
    }

    /**
     * 将对象加入到缓存
     *
     * @param key   缓存标识
     * @param value 缓存实例对象
     */
    default void putValue(K key, V value) {
        putValue(key, value, timeToLive());
    }

    /**
     * 将对象加入到缓存，并设置失效时长，单位秒
     *
     * @param key      缓存标识
     * @param value    缓存实例对象
     * @param duration 时效(单位: 秒)
     */
    default void putValue(K key, V value, long duration) {
        putValue(key, value, duration, timeUnit());
    }

    /**
     * 将对象加入到缓存，并设置失效时长
     *
     * @param key      缓存标识
     * @param value    缓存实例对象
     * @param duration 时效
     * @param unit     时效单位
     */
    void putValue(K key, V value, long duration, TimeUnit unit);

    /**
     * 批量插入数据
     *
     * @param elements objects to be put in cache
     */
    default void putValue(Map<K, V> elements) {
        elements.forEach(this::putValue);
    }

    /**
     * 清除指定缓存
     *
     * @param key 缓存标识
     */
    void evict(K key);

    /**
     * 清除所有缓存
     */
    void clear();
}
