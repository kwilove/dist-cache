package com.zjhuang.distcache.cache.config.proxy;

import com.zjhuang.distcache.cache.AbstractCache;
import com.zjhuang.distcache.cache.sweeper.CacheClearMessageSender;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

/**
 * 生产缓存代理对象的工厂类
 *
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/14 11:26 上午
 * @see CacheProxy
 **/
public class CacheProxyFactory {

    // 自定义的BeanId前缀
    public static final String PROXY_PREFIX = "CacheProxy@";
    private final Object lock = new Object();

    private final ConfigurableListableBeanFactory configurableListableBeanFactory;
    private final CacheClearMessageSender cacheClearMessageSender;

    public CacheProxyFactory(ConfigurableListableBeanFactory configurableListableBeanFactory,
                             CacheClearMessageSender cacheClearMessageSender) {
        this.configurableListableBeanFactory = configurableListableBeanFactory;
        this.cacheClearMessageSender = cacheClearMessageSender;
    }

    /**
     * @param cacheClass 被代理对象类型
     * @return 获取缓存代理对象
     */
    public Object getCacheProxy(Class<?> cacheClass) {
        String proxyBeanId = getCacheProxyId(cacheClass);
        Object cacheProxy;
        // double check
        if (configurableListableBeanFactory.containsBean(proxyBeanId)) {
            cacheProxy = configurableListableBeanFactory.getBean(proxyBeanId);
        } else {
            synchronized (lock) {
                if (configurableListableBeanFactory.containsBean(proxyBeanId)) {
                    cacheProxy = configurableListableBeanFactory.getBean(proxyBeanId);
                } else {
                    Constructor<?>[] declaredConstructors = cacheClass.getDeclaredConstructors();
                    // 根据构造方法类型，选择创建代理的方式，暂时优先考虑无参构造方法
                    if (declaredConstructors[0].getParameterCount() == 0) {
                        // 创建 Cache 对象代理
                        cacheProxy = new CacheProxy(cacheClass, cacheClearMessageSender).create();
                    } else {
                        // 有参构造器创建代理
                        Parameter[] parameters = declaredConstructors[0].getParameters();
                        Class<?>[] parameterTypes = declaredConstructors[0].getParameterTypes();
                        Object[] arguments = new Object[parameterTypes.length];
                        for (int i = 0; i < parameterTypes.length; i++) {
                            if (AbstractCache.class.isAssignableFrom(parameterTypes[i])) {
                                if (cacheClass.equals(parameterTypes[i])) {
                                    // 自依赖报错
                                    throw new BeanCurrentlyInCreationException(String.format("Can not support injecting itself, bean class [%s]", cacheClass));
                                }
                                // 注入 AbstractCache 实例
                                arguments[i] = this.getCacheProxy(parameterTypes[i]);
                            } else {
                                try {
                                    // 这里对其他bean属性的构造注入，能通过getBean拿到最好，拿不到就交给autowireBeanProperties去自动注入
                                    arguments[i] = configurableListableBeanFactory.getBean(parameters[i].getName(), parameterTypes[i]);
                                } catch (NoSuchBeanDefinitionException e) {
                                    arguments[i] = configurableListableBeanFactory.getBean(parameterTypes[i]);
                                }
                            }
                        }
                        cacheProxy = new CacheProxy(cacheClass, cacheClearMessageSender).create(parameterTypes, arguments);
                    }
                    // 生成新的function cache类型缓存，并按照ByName注册到ioc容器
                    cacheProxy = configurableListableBeanFactory.initializeBean(cacheProxy, proxyBeanId);
                    configurableListableBeanFactory.autowireBeanProperties(cacheProxy, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
                    configurableListableBeanFactory.registerSingleton(proxyBeanId, cacheProxy);
                }
            }
        }
        return cacheProxy;
    }

    /**
     * 生成代理对象名称，将作为BeanName
     *
     * @param targetCacheClass 被代理目标缓存对象类型
     * @return 代理对象名称
     */
    private String getCacheProxyId(Class<?> targetCacheClass) {
        return PROXY_PREFIX + targetCacheClass.getName();
    }
}
