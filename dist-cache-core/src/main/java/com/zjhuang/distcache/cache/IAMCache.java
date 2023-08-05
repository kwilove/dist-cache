package com.zjhuang.distcache.cache;

import com.zjhuang.distcache.cache.config.storage.CacheStorage;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.function.Function;

/**
 * iam缓存抽象，做到业务层防腐，中间件透明
 * <p>
 * Created by hedy on 2023/2/16.
 *
 * @param <K>
 * @param <V>
 */
public interface IAMCache<K, V> extends ICache<K, V> {

    /**
     * 缓存组件名称
     * <p>
     * 建议在 {@link com.zjhuang.distcache.cache.impl.IAMCacheNameEnums} 中定义和引用
     *
     * @return 名称
     */
    String getCacheName();

    /**
     * 多级缓存机制下，需要实现类主动注入一个 CacheStorage 对象
     *
     * @return 获取缓存存储对象
     */
    default CacheStorage getCacheStorage() {
        return null;
    }

    /**
     * 从缓存中获得对象，当对象不在缓存中或已经过期返回null
     * <p>
     * PS：个人建议，这种方法不适合声明在IAMCache，cache key应该是一个确定的值，
     * 对key的包装行为可以在外部调用侧先完成，再将包装结果传递进来
     *
     * @param arguments 缓存key所需参数
     * @return 缓存实例对象
     */
    default V getValueByFormatKey(Object... arguments) {
        return this.getValue((K) formatCacheKey(arguments));
    }

    /**
     * 从缓存中获得对象，当对象不在缓存中或已经过期返回null
     * <p>
     * 有点类似于{@link java.util.Map#getOrDefault(Object, Object)}
     *
     * @param key             缓存标识
     * @param mappingFunction value映射
     * @return 缓存实例对象
     */
    V getValue(K key, Function<K, V> mappingFunction);

    /**
     * 重新加载缓存
     *
     * @param key 缓存标识
     * @return 缓存实例对象
     */
    default V reload(K key) {
        evict(key);
        return getValue(key);
    }

    /**
     * 获取缓存key规则
     * <p>
     * 表达式使用{@link MessageFormat#format}解析
     * eg: iam:{模块}:{业务}:{xxx}
     *
     * @return 缓存key规则
     */
    String cacheKeyPattern();

    /**
     * 格式化成有效的缓存key
     *
     * @param arguments 参数
     * @return 有效缓存key
     */
    default String formatCacheKey(Object... arguments) {
        return MessageFormat.format(cacheKeyPattern(), arguments);
    }

    /**
     * 缓存包装对象
     * <p>
     * 主要用于手动管理失效逻辑，弥补本地缓存框架不可以根据key设置不同失效时间的功能
     *
     * @param <V>
     */
    class CacheObjectWrapper<V> implements Serializable {

        private static final long serialVersionUID = 4548355891040981233L;

        /**
         * 缓存标识
         */
        private final Object key;

        /**
         * 缓存对象实例
         */
        private final V value;

        /**
         * 有效时间(time to live) 0表示永久存活
         */
        private final long ttl;

        /**
         * 最新时间
         */
        private final long lastTime;

        public CacheObjectWrapper(Object key, V value) {
            this.key = key;
            this.value = value;
            this.ttl = 0;
            this.lastTime = System.currentTimeMillis();
        }

        public CacheObjectWrapper(Object key, V value, long ttl) {
            this.key = key;
            this.value = value;
            this.ttl = ttl;
            this.lastTime = System.currentTimeMillis();
        }

        public Object getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public long getTtl() {
            return ttl;
        }

        public long getLastTime() {
            return lastTime;
        }

        /**
         * 获取失效时间
         *
         * @return 返回时间戳
         */
        public long getExpireTime() {
            return ttl + lastTime;
        }
    }

}
