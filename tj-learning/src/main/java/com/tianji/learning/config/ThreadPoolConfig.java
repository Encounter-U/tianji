package com.tianji.learning.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Encounter
 * @date 2024/11/22 19:38<br/>
 */
@Slf4j
@Configuration
public class ThreadPoolConfig
    {
        /**
         * 用来处理学习记录延迟任务的线程池
         * <pre>
         *     1. 核心线程数：8
         *     2. 最大线程数：10
         *     3. 临时线程存活时间： 60s
         *     4. 任务阻塞队列容量：4
         *     5. 拒绝策略：CallerRunsPolicy
         *     6. 线程工厂：Executors.defaultThreadFactory()
         * </pre>
         *
         * @return {@link ThreadPoolExecutor } 线程池
         */
        @Bean("learningRecordThreadPool")
        public ThreadPoolTaskExecutor learningRecordThreadPool()
            {
                log.info("开始初始化学习记录延迟任务处理线程池...");
                ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
                //配置核心线程数
                executor.setCorePoolSize(8);
                //配置最大线程数
                executor.setMaxPoolSize(10);
                //配置队列大小
                executor.setQueueCapacity(999);
                //配置线程池中的线程的名称前缀
                executor.setThreadNamePrefix("learning-record-");
                //临时队列存活时间
                executor.setKeepAliveSeconds(60);
                
                // 设置拒绝策略：当pool已经达到max size的时候，如何处理新任务
                // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
                executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
                //执行初始化
                executor.initialize();
                return executor;
            }
    }
