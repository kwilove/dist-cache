package com.zjhuang.distcache.proxy;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.ReflectionUtils;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/14 1:42 下午
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class SpringBeanReferenceProcessor implements BeanPostProcessor {

    private final List<InjectSuggest> injectSuggests;

    public SpringBeanReferenceProcessor(List<InjectSuggest> injectSuggests) {
        this.injectSuggests = injectSuggests.stream()
            .sorted(Comparator.comparingInt(InjectSuggest::getOrder))
            .collect(Collectors.toList());
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, @NotNull String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        ReflectionUtils.doWithFields(beanClass, field -> injectFunctionField(bean, field));
        return bean;
    }

    private void injectFunctionField(Object bean, Field field) {
        Class<?> fieldType = field.getType();
        ReflectionUtils.makeAccessible(field);
        Object currentValue = ReflectionUtils.getField(field, bean);
        for (InjectSuggest injectSuggest : injectSuggests) {
            if (injectSuggest.support(fieldType)) {
                Object proxy = injectSuggest.getValue(fieldType);
                if (proxy == null || currentValue == proxy) {
                    break;
                }
                ReflectionUtils.setField(field, bean, proxy);
            }
        }
    }
}
