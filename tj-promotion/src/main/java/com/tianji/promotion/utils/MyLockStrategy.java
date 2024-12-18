package com.tianji.promotion.utils;

import org.redisson.api.RLock;

/**
 * <p>锁失败策略</p>
 *
 * @author Encounter
 * @date 2024/12/12 20:49<br/>
 */
public enum MyLockStrategy
    {
        
        /**
         * 快速结束
         */
        SKIP_FAST()
                    {
                        @Override
                        public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException
                            {
                                return lock.tryLock(0, prop.leaseTime(), prop.unit());
                            }
                    },
        
        /**
         * 快速失败
         */
        FAIL_FAST()
                    {
                        @Override
                        public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException
                            {
                                boolean isLock = lock.tryLock(0, prop.leaseTime(), prop.unit());
                                if (!isLock)
                                    {
                                        throw new RuntimeException("请求过于频繁");
                                    }
                                return true;
                            }
                    },
        
        /**
         * 无限重试
         */
        KEEP_TRYING()
                    {
                        @Override
                        public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException
                            {
                                lock.lock(prop.leaseTime(), prop.unit());
                                return true;
                            }
                    },
        
        /**
         * 重试超时后结束
         */
        SKIP_AFTER_RETRY_TIMEOUT()
                    {
                        @Override
                        public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException
                            {
                                return lock.tryLock(prop.waitTime(), prop.leaseTime(), prop.unit());
                            }
                    },
        
        /**
         * 重试超时后失败
         */
        FAIL_AFTER_RETRY_TIMEOUT()
                    {
                        @Override
                        public boolean tryLock(RLock lock, MyLock prop) throws InterruptedException
                            {
                                boolean isLock = lock.tryLock(prop.waitTime(), prop.leaseTime(), prop.unit());
                                if (!isLock)
                                    {
                                        throw new RuntimeException("请求过于频繁");
                                    }
                                return true;
                            }
                    };
        
        /**
         * 尝试获取锁
         *
         * @param lock 锁
         * @param prop 属性
         * @return boolean 是否锁定成功
         * @throws InterruptedException 打断异常
         */
        public abstract boolean tryLock(RLock lock, MyLock prop) throws InterruptedException;
    }
