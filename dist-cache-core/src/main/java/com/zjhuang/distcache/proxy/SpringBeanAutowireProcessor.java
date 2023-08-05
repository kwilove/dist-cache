package com.zjhuang.distcache.proxy;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/14 1:42 下午
 */
public class SpringBeanAutowireProcessor implements BeanFactoryPostProcessor {

    private final List<InjectSuggest> injectSuggests;

    public SpringBeanAutowireProcessor(List<InjectSuggest> injectSuggests) {
        this.injectSuggests = injectSuggests.stream()
            .sorted(Comparator.comparingInt(InjectSuggest::getOrder))
            .collect(Collectors.toList());
    }

    @Override
    public void postProcessBeanFactory(@NotNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        defaultListableBeanFactory.setAutowireCandidateResolver(
            new CacheProxyAutowireCandidateResolver(
                defaultListableBeanFactory.getAutowireCandidateResolver()
            )
        );
    }

    class CacheProxyAutowireCandidateResolver extends ContextAnnotationAutowireCandidateResolver {

        private final AutowireCandidateResolver delegate;

        CacheProxyAutowireCandidateResolver(AutowireCandidateResolver delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean isAutowireCandidate(@NotNull BeanDefinitionHolder bdHolder,
                                           @NotNull DependencyDescriptor descriptor) {
            if (delegate != null) {
                return delegate.isAutowireCandidate(bdHolder, descriptor);
            }
            return super.isAutowireCandidate(bdHolder, descriptor);
        }

        @Override
        public boolean isRequired(@NotNull DependencyDescriptor descriptor) {
            if (delegate != null) {
                return delegate.isRequired(descriptor);
            }
            return super.isRequired(descriptor);
        }

        @Override
        public boolean hasQualifier(@NotNull DependencyDescriptor descriptor) {
            if (delegate != null) {
                return delegate.hasQualifier(descriptor);
            }
            return super.hasQualifier(descriptor);
        }

        @Override
        @Nullable
        public Object getLazyResolutionProxyIfNecessary(@NotNull DependencyDescriptor descriptor,
                                                        String beanName) {
            if (delegate != null) {
                return delegate.getLazyResolutionProxyIfNecessary(descriptor, beanName);
            }
            return super.getLazyResolutionProxyIfNecessary(descriptor, beanName);
        }

        @Override
        public @NotNull AutowireCandidateResolver cloneIfNecessary() {
            if (delegate != null) {
                return delegate.cloneIfNecessary();
            }
            return super.cloneIfNecessary();
        }

        @Override
        public Object getSuggestedValue(@NotNull DependencyDescriptor descriptor) {
            Class<?> type = descriptor.getDeclaredType();
            // TODO 这里可以做个 cache 加快启动速度
            for (InjectSuggest injectSuggest : injectSuggests) {
                if (!injectSuggest.support(type)) {
                    continue;
                }
                return injectSuggest.getValue(type);
            }
            if (delegate != null) {
                return delegate.getSuggestedValue(descriptor);
            }
            return super.getSuggestedValue(descriptor);
        }
    }
}
