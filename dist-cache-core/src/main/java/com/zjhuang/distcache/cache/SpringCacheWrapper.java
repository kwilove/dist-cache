package com.zjhuang.distcache.cache;

import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Spring Cache包装类
 * <p>
 * 参考{@link org.springframework.cache.caffeine.CaffeineCache}实现
 * <p>
 * Created by hedy on 2023/2/16.
 */
@SuppressWarnings("all")
public class SpringCacheWrapper extends AbstractValueAdaptingCache {

    private final IAMCache cache;

    public SpringCacheWrapper(IAMCache cache) {
        super(true);
        this.cache = cache;
    }

    @Override
    protected Object lookup(Object key) {
        return this.cache.getValue(key);
    }

    @Override
    public String getName() {
        return this.cache.getCacheName();
    }

    @Override
    public Object getNativeCache() {
        return this.cache;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return (T) fromStoreValue(this.cache.getValue(key, new LoadFunction(valueLoader)));
    }

    @Override
    public void put(Object key, Object value) {
        this.cache.putValue(key, value);
    }

    @Override
    public void evict(Object key) {
        this.cache.evict(key);
    }

    @Override
    public void clear() {
        this.cache.clear();
    }

    private class LoadFunction implements Function<Object, Object> {

        private final Callable<?> valueLoader;

        public LoadFunction(Callable<?> valueLoader) {
            this.valueLoader = valueLoader;
        }

        @Override
        public Object apply(Object o) {
            try {
                return toStoreValue(this.valueLoader.call());
            } catch (Exception ex) {
                throw new ValueRetrievalException(o, this.valueLoader, ex);
            }
        }
    }
}
