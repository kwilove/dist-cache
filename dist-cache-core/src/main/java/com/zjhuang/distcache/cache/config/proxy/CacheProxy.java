package com.zjhuang.distcache.cache.config.proxy;

import com.zjhuang.distcache.cache.IAMCache;
import com.zjhuang.distcache.cache.sweeper.CacheClearMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.NoOp;

/**
 * 缓存代理类
 * <p>
 * 增强{@link com.zjhuang.distcache.cache.AbstractCache}实例的{@code evict}和{@code clear}方法，
 * 通过redis的pub/sub能力实现多实例内存级缓存失效
 *
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/13 8:06 下午
 * @see com.zjhuang.distcache.cache.AbstractCache
 **/
@Slf4j
public class CacheProxy extends Enhancer {

    private final CacheClearMessageSender cacheClearMessageSender;

    public CacheProxy(Class<?> cacheClass, CacheClearMessageSender cacheClearMessageSender) {
        super();
        this.setSuperclass(cacheClass);
        // 定义需要代理的方法和method callback
        String[] callbackNames = {
            "evict",
            "clear"
        };
        Callback[] callbacks = {
            evict(),
            clear(),
            NoOp.INSTANCE
        };
        this.setCallbacks(callbacks);
        this.setCallbackFilter(method -> {
            String methodName = method.getName();
            for (int i = 0; i < callbackNames.length; i++) {
                if (callbackNames[i].equals(methodName)) {
                    return i;
                }
            }
            // else NoOp.INSTANCE
            return callbackNames.length;
        });

        this.cacheClearMessageSender = cacheClearMessageSender;
    }

    private MethodInterceptor evict() {
        return (obj, method, args, methodProxy) -> {
            Object key = args.length == 1 ? args[0] : args;
            methodProxy.invokeSuper(obj, args);
            cacheClearMessageSender.evict((IAMCache) obj, key);
            return null;
        };
    }

    private MethodInterceptor clear() {
        return (obj, method, args, methodProxy) -> {
            methodProxy.invokeSuper(obj, args);
            cacheClearMessageSender.clear((IAMCache) obj);
            return null;
        };
    }
}
