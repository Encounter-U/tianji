package com.tianji.promotion.utils;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Encounter
 * @date 2024/12/12 20:37<br/>
 */
@Component
public class MyLockFactory
    {
        private final Map<MyLockType, Function<String, RLock>> lockHandlers;
        
        public MyLockFactory(RedissonClient redissonClient)
            {
                this.lockHandlers = new EnumMap<>(MyLockType.class);
                this.lockHandlers.put(MyLockType.RE_ENTER_LOCK, redissonClient::getLock);
                this.lockHandlers.put(MyLockType.FAIR_LOCK, redissonClient::getFairLock);
                this.lockHandlers.put(MyLockType.READ_LOCK, key -> redissonClient.getReadWriteLock(key).readLock());
                this.lockHandlers.put(MyLockType.WRITE_LOCK, key -> redissonClient.getReadWriteLock(key).writeLock());
            }
        
        /**
         * 获取锁定
         *
         * @param type 类型
         * @param key  键
         * @return {@link RLock }
         */
        public RLock getLock(MyLockType type, String key)
            {
                return lockHandlers.get(type).apply(key);
            }
    }
