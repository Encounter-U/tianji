package com.tianji.promotion.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * <p>自定义锁注解</p>
 *
 * @author Encounter
 * @date 2024/12/12 19:28<br/>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MyLock
    {
        /**
         * 锁名称
         *
         * @return {@link String }
         */
        String name();
        
        /**
         * 等待时间 默认1秒
         *
         * @return long
         */
        long waitTime() default 1;
        
        /**
         * 锁自动释放时间 默认-1 不自动释放
         *
         * @return long
         */
        long leaseTime() default -1;
        
        /**
         * 时间单位 默认秒
         *
         * @return {@link TimeUnit }
         */
        TimeUnit unit() default TimeUnit.SECONDS;
        
        /**
         * 锁类型 默认可重入锁
         *
         * @return {@link MyLockType }
         */
        MyLockType lockType() default MyLockType.RE_ENTER_LOCK;
        
        /**
         * 失败处理策略 默认重试超时后失败
         *
         * @return {@link MyLockStrategy }
         */
        MyLockStrategy lockStrategy() default MyLockStrategy.FAIL_AFTER_RETRY_TIMEOUT;
    }
