package com.zjhuang.distcache.cache.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.cache.annotation.Cacheable;

/**
 * IAM内部缓存资源类型
 * <p>
 * 将 enum 更换成 final class，因为 spring cache 注解不适合引用 enum，比如 {@link Cacheable#cacheNames()}
 * Created by hedy on 2023/2/16.
 *
 * @author huangzijing
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IAMCacheNameEnums {

    /**
     * 图形验证码缓存
     */
    public static final String CAPTCHA_CODE = "CAPTCHA_CODE";

    /**
     * 通知模板缓存
     */
    public static final String NOTICE_TEMPLATE = "NOTICE_TEMPLATE";

    /**
     * 通知类型验证码(sms、email)缓存
     */
    public static final String NOTICE_VERIFY_CODE = "NOTICE_VERIFY_CODE";

    /**
     * 通知类型验证码限流器缓存
     */
    public static final String NOTICE_RATE_LIMITER = "NOTICE_RATE_LIMITER";

    /**
     * 用户缓存
     */
    public static final String USER = "USER";

    /**
     * 用户秘钥
     */
    public static final String USER_SECRET = "USER_SECRET";

    /**
     * 已登录用户缓存
     */
    public static final String LOGGED_USER = "LOGGED_USER";

    /**
     * 权限配置
     */
    public static final String PERMISSION_CONFIG = "PERMISSION_CONFIG";

    /**
     * 角色关联
     */
    public static final String ROLE_RELATION_BY_BIZ_TYPE_AND_BIZ_ID = "ROLE_RELATION_BY_BIZ_TYPE_AND_BIZ_ID";

    /**
     * 角色授权
     */
    public static final String PERMISSION_GRANT_BY_TARGET = "PERMISSION_GRANT_BY_TARGET";

}
