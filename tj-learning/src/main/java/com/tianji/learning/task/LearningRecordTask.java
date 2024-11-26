package com.tianji.learning.task;

import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.mapper.LearningRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Encounter
 * @date 2024/11/23 15:26<br/>
 * 学习记录任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LearningRecordTask
    {
        private final static String RECORD_KEY_TEMPLATE = "learning:record:{}";
        private final StringRedisTemplate redisTemplate;
        private final LearningRecordMapper recordMapper;
        
        /**
         * 持久化学习记录 每30s写入数据库一次
         */
        @Scheduled(cron = "*/20 * * * * ?")
        public void persistentLearningRecords()
            {
                //获取缓存中所有的学习记录
                Set<Object> records = redisTemplate.opsForHash().keys(RECORD_KEY_TEMPLATE);
                if (records.isEmpty())
                    {
                        log.info("没有学习记录需要持久化");
                        return;
                    }
                //将学习记录写入数据库
                for (Object record : records.toArray())
                    {
                        LearningRecord learningRecord = (LearningRecord) redisTemplate.opsForHash().get(RECORD_KEY_TEMPLATE, record);
                        recordMapper.insert(learningRecord);
                    }
            }
    }
