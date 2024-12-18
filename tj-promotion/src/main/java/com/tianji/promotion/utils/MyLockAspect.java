package com.tianji.promotion.utils;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * @author Encounter
 * @date 2024/12/12 19:32<br/>
 */
@Component
@Aspect
@RequiredArgsConstructor
public class MyLockAspect implements Ordered
    {
        private final RedissonClient redissonClient;
        private final MyLockFactory lockFactory;
        
        @Around("@annotation(myLock)")
        public Object around(ProceedingJoinPoint joinPoint, MyLock myLock) throws Throwable
            {
                //创建锁
                RLock lock = lockFactory.getLock(myLock.lockType(), myLock.name());
                //尝试获取锁
                boolean isLock = myLock.lockStrategy().tryLock(lock, myLock);
                //判断是否成功
                if (!isLock)
                    {
                        //失败处理策略已封装到lockStrategy中,此处直接结束，无需处理
                        return null;
                    }
                try
                    {
                        //执行业务逻辑
                        return joinPoint.proceed();
                    }
                finally
                    {
                        //释放锁
                        lock.unlock();
                    }
            }
        
        /**
         * 获取顺序 0 优先级最高
         *
         * @return int
         */
        @Override
        public int getOrder()
            {
                return 0;
            }
    }
