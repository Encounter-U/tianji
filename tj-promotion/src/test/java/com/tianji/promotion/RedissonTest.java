package com.tianji.promotion;

import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Encounter
 * @date 2024/12/12 19:18<br/>
 */
@SpringBootTest(classes = PromotionApplication.class)
public class RedissonTest
    {
        @Autowired
        private RedissonClient redissonClient;
        
        @Test
        void testRedisson()
            {
                //获取锁对象，指定锁名称
                RLock lock = redissonClient.getLock("anyLock");
                try
                    {
                        //尝试获取锁，参数：waitTime:等待时间，leaseTime:锁自动释放时间，unit:时间单位
                        boolean success = lock.tryLock(10, 10, java.util.concurrent.TimeUnit.SECONDS);
                        if (success)
                            {
                                //获取锁成功，执行业务逻辑
                                System.out.println("获取锁成功");
                            }
                        else
                            {
                                //获取锁失败
                                System.out.println("获取锁失败");
                            }
                    }
                catch (InterruptedException e)
                    {
                        throw new RuntimeException(e);
                    }
                finally
                    {
                        //释放锁
                        lock.unlock();
                    }
            }
    }
