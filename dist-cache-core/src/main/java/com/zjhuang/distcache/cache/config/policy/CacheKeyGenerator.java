package com.zjhuang.distcache.cache.config.policy;

/**
 * cache key 生成器
 *
 * @author 黄子敬（hzj273812@alibaba-inc.com）
 * 2023/4/11 5:17 下午
 **/
public interface CacheKeyGenerator {

    /**
     * 在使用 redis 这类中心化存储服务时，出于数据隔离需要，比如环境数据隔离、虽然也可以通过database隔离，
     * 但是像华为云代理架构默认关闭database设置，机制上提供一个region隔离就很有必要
     * 需要通过在key拼接region前缀实现cache的隔离
     *
     * @return 返回缓存Region
     */
    default String getRegion() {
        return null;
    }

    /**
     * @return 获取命名空间
     */
    default String getNamespace() {
        return null;
    }

    /**
     * 设置命名空间
     *
     * @param namespace 命名空间
     */
    void setNamespace(String namespace);

    /**
     * 格式化key = {region}:{namespace}:{key}
     *
     * @param key 原始缓存key
     * @return 格式化后缓存key
     */
    default String formatKey(String key) {
        StringBuilder sb = new StringBuilder();
        if (null != getRegion() && getRegion().length() > 0) {
            sb.append(getRegion()).append(":");
        }
        if (null != getNamespace() && getNamespace().length() > 0) {
            sb.append(getNamespace()).append(":");
        }
        return sb.append(key).toString();
    }
}
