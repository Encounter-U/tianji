package com.tianji.promotion.utils;

import com.tianji.common.utils.BooleanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author Encounter
 * @date 2024/12/12 15:08<br/>
 */
@RequiredArgsConstructor
public class RedisLock
    {
        private final String key;
        private final StringRedisTemplate redisTemplate;
        
        /**
         * 尝试获取锁
         *
         * @param leaseTime 锁自动释放时间
         * @param unit      时间单位
         * @return boolean  是否获取锁成功 true:成功 false:失败
         */
        public boolean tryLock(long leaseTime, TimeUnit unit)
            {
                //获取线程名称
                String value = Thread.currentThread().getName();
                //获取锁
                Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, leaseTime, unit);
                //获取锁成功
                return BooleanUtils.isTrue(success);
            }
        
        /**
         * 释放锁
         */
        public void unlock()
            {
                redisTemplate.delete(key);
            }
    }
