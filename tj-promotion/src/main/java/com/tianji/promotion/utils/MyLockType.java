package com.tianji.promotion.utils;

/**
 * <p>锁类型</p>
 *
 * @author Encounter
 * @date 2024/12/12 20:32<br/>
 */
public enum MyLockType
    {
        /**
         * 可重入锁
         */
        RE_ENTER_LOCK,
        
        /**
         * 公平锁
         */
        FAIR_LOCK,
        
        /**
         * 读锁
         */
        READ_LOCK,
        
        /**
         * 写锁
         */
        WRITE_LOCK
        
    }
