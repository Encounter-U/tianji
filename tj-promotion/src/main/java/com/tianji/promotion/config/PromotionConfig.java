package com.tianji.promotion.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Encounter
 * @date 2024/12/06 14:16<br/>
 */
@Slf4j
@Configuration
public class PromotionConfig
    {
        @Bean
        public Executor generateExchangeCodeExecutor()
            {
                ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
                //核心线程池大小
                executor.setCorePoolSize(8);
                //最大线程池大小
                executor.setMaxPoolSize(10);
                //队列容量
                executor.setQueueCapacity(200);
                //队列名称
                executor.setThreadNamePrefix("exchange-code-handle-");
                //拒绝策略
                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
                //初始化
                executor.initialize();
                return executor;
            }
    }
