package com.zjhuang.distcache.proxy;

import org.springframework.core.Ordered;

/**
 * 注入建议
 * 会在 BeanFactoryProcessor 时机执行，注意 bean 依赖
 *
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/14 1:42 下午
 */
public interface InjectSuggest extends Ordered {
    /**
     * @param type 待注入的类型
     * @return 是否支持注入该类型
     */
    boolean support(Class<?> type);

    /**
     * @param type 待注入的类型
     * @return 注入实例
     */
    Object getValue(Class<?> type);

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
