package com.tianji.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author Encounter
 * @date 2024/11/27 16:06<br/>
 * Caffeine缓存测试
 */
@Slf4j
//@SpringBootTest(classes = CaffeineTest.class)
public class CaffeineTest
    {
        @Test
        void testBasicOps()
            {
                Cache<String, String> cache = Caffeine.newBuilder()
                        .maximumSize(2) //最大缓存数量
                        .expireAfterWrite(5, TimeUnit.SECONDS) //写入后5秒过期
                        .build();
                
                //添加缓存
                cache.put("Caffeine缓存测试", "Hello, Caffeine!");
                
                //获取缓存
                String caffeine = cache.getIfPresent("Caffeine缓存测试");
                
                log.info("Caffeine缓存测试: {}", caffeine);
                
                //删除缓存
                cache.invalidate("Caffeine缓存测试");
                
                //获取缓存，解释下面的代码
                // 1. 如果缓存中存在key为"Caffeine缓存测试"的缓存，则返回该缓存
                // 2. 如果缓存中不存在key为"Caffeine缓存测试"的缓存，则调用key -> "Hello, Caffeine!"，将返回值作为缓存值
                // 3. 返回值可改为查询数据库的逻辑
                String defaultCaffeine = cache.get("Caffeine缓存测试", key -> "Hello, Here's the default Caffeine!");
                log.info("Caffeine默认缓存测试: {}", defaultCaffeine);
            }
    }
