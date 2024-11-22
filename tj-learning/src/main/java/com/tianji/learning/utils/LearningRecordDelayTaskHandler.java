package com.tianji.learning.utils;

import cn.hutool.json.JSONUtil;
import com.tianji.common.utils.JsonUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.DelayQueue;

/**
 * @author Encounter
 * @date 2024/11/22 14:05<br/>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LearningRecordDelayTaskHandler
    {
        private final static String RECORD_KEY_TEMPLATE = "learning:record:{}";
        private static volatile boolean begin = true;
        private final StringRedisTemplate redisTemplate;
        private final LearningRecordMapper recordMapper;
        private final ILearningLessonService lessonService;
        private final DelayQueue<DelayTask<RecordTaskData>> queue = new DelayQueue<>();
        //引入线程池
        @Resource(name = "learningRecordThreadPool")
        private ThreadPoolTaskExecutor learningRecordThreadPool;
        
        /**
         * 初始化延迟任务处理器
         */
        @PostConstruct
        public void init()
            {
                //CompletableFuture.runAsync(this::handleDelayTask);
                learningRecordThreadPool.execute(this::handleDelayTask);
            }
        
        /**
         * 销毁延迟任务处理器
         */
        @PreDestroy
        void destroy()
            {
                begin = false;
                learningRecordThreadPool.shutdown();
                log.info("销毁延迟任务处理器...");
            }
        
        /**
         * 处理延迟任务
         */
        private void handleDelayTask()
            {
                while (begin)
                    {
                        try
                            {
                                //获取到期的延迟任务
                                DelayTask<RecordTaskData> task = queue.take();
                                //获取任务数据
                                RecordTaskData data = task.getData();
                                //查询Redis缓存
                                LearningRecord record = readRecordCache(data.getLessonId(), data.getSectionId());
                                //缓存为空，执行下一次任务
                                if (record == null)
                                    {
                                        continue;
                                    }
                                //比较任务时间和缓存时间
                                if (!Objects.equals(data.getMoment(), record.getMoment()))
                                    {
                                        //二者不一样，说明用户还在继续提交学习记录，重新加入延迟队列
                                        continue;
                                    }
                                
                                //二者一致，更新学习记录
                                record.setFinished(null);
                                recordMapper.updateById(record);
                                //更新课表最近学习信息
                                LearningLesson lesson = new LearningLesson();
                                lesson.setId(data.getLessonId());
                                lesson.setLatestSectionId(data.getSectionId());
                                lesson.setLatestLearnTime(LocalDateTime.now());
                                lessonService.updateById(lesson);
                            }
                        catch (Exception e)
                            {
                                log.error("处理延迟任务异常", e);
                            }
                    }
            }
        
        /**
         * 读取学习记录缓存
         *
         * @param lessonId  课id
         * @param sectionId 章节id
         * @return {@link LearningRecord } 学习记录
         */
        public LearningRecord readRecordCache(Long lessonId, Long sectionId)
            {
                try
                    {
                        String key = StringUtils.format(RECORD_KEY_TEMPLATE, lessonId);
                        Object recordData = redisTemplate.opsForHash().get(key, sectionId.toString());
                        if (recordData == null)
                            return null;
                        // 解析缓存数据 返回学习记录
                        return JsonUtils.toBean(recordData.toString(), LearningRecord.class);
                    }
                catch (Exception e)
                    {
                        log.error("读取学习记录缓存异常", e);
                        return null;
                    }
            }
        
        /**
         * 写入学习记录缓存
         *
         * @param record 学习记录
         */
        public void writeRecordCache(LearningRecord record)
            {
                log.debug("写入学习记录缓存：{}", record);
                try
                    {
                        //设置缓存key
                        String key = StringUtils.format(RECORD_KEY_TEMPLATE, record.getLessonId());
                        //写入缓存 key , sectionId , record
                        redisTemplate.opsForHash().put(key, record.getSectionId().toString(),
                                JSONUtil.toJsonStr(new RecordCacheData(record)));
                        //设置过期时间1分钟
                        redisTemplate.expire(key, Duration.ofMinutes(1));
                    }
                catch (Exception e)
                    {
                        log.error("写入学习记录缓存异常", e);
                    }
                
            }
        
        /**
         * 添加延迟任务
         *
         * @param record 学习记录
         */
        public void addDelayTask(LearningRecord record)
            {
                //加到队列前先写入缓存
                writeRecordCache(record);
                //加入延迟队列
                queue.put(new DelayTask<>(new RecordTaskData(record), Duration.ofSeconds(20)));
            }
        
        /**
         * 清理缓存
         *
         * @param lessonId  课id
         * @param sectionId 章节id
         */
        public void cleanCache(Long lessonId, Long sectionId)
            {
                String key = StringUtils.format(RECORD_KEY_TEMPLATE, lessonId);
                redisTemplate.opsForHash().delete(key, sectionId.toString());
            }
        
        /**
         * 记录任务数据
         *
         * @author Encounter
         * @date 2024/11/22
         */
        @Data
        @NoArgsConstructor
        public static class RecordTaskData
            {
                private Long lessonId;
                private Long sectionId;
                private Integer moment;
                
                public RecordTaskData(LearningRecord record)
                    {
                        this.lessonId = record.getLessonId();
                        this.sectionId = record.getSectionId();
                        this.moment = record.getMoment();
                    }
            }
        
        /**
         * 记录缓存数据
         *
         * @author Encounter
         * @date 2024/11/22
         */
        @Data
        @NoArgsConstructor
        public static class RecordCacheData
            {
                private Long id;
                private Integer moment;
                private boolean finished;
                
                public RecordCacheData(LearningRecord record)
                    {
                        this.id = record.getId();
                        this.moment = record.getMoment();
                        this.finished = record.getFinished();
                    }
            }
    }
