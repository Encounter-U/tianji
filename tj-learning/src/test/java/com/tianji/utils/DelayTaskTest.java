package com.tianji.utils;

import com.tianji.learning.utils.DelayTask;
import groovy.util.logging.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.DelayQueue;

/**
 * @author Encounter
 * @date 2024/11/22 13:53<br/>
 */
@Slf4j
//@SpringBootTest(classes = DelayTaskTest.class)
public class DelayTaskTest
    {
        private static final Logger log = LoggerFactory.getLogger(DelayTaskTest.class);
        
        @Test
        void testDelayQueue() throws InterruptedException
            {
                //初始化延迟队列
                DelayQueue<DelayTask<String>> queue = new DelayQueue<>();
                //添加延迟任务
                log.info("开始执行延迟队列任务...");
                queue.add(new DelayTask<>("延迟任务3", Duration.ofSeconds(3)));
                queue.add(new DelayTask<>("延迟任务1", Duration.ofSeconds(1)));
                queue.add(new DelayTask<>("延迟任务2", Duration.ofSeconds(2)));
                
                //开始执行任务
                while (!queue.isEmpty())
                    {
                        //获取延迟任务
                        DelayTask<String> task = queue.take();
                        log.info("执行任务：{}", task.getData());
                    }
            }
    }
