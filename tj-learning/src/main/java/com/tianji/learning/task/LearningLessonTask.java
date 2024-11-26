package com.tianji.learning.task;

import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.service.ILearningLessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Encounter
 * @date 2024/11/21 16:47<br/>
 * 检查课程是否过期
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LearningLessonTask
    {
        private final ILearningLessonService lessonService;
        
        /**
         * 检查课程是否过期
         * 每天凌晨1点触发一次
         */
        @Scheduled(cron = "0 0 1 * * ?")
        public void checkLessonExpired()
            {
                log.info("开始检查课程是否过期");
                List<LearningLesson> list = lessonService.list();
                if (list == null || list.isEmpty())
                    {
                        log.info("没有课程");
                        return;
                    }
                for (LearningLesson lesson : list)
                    {
                        if (lesson.getExpireTime().isBefore(LocalDateTime.now()) && lesson.getStatus() != LessonStatus.EXPIRED)
                            {
                                log.info("课程{}已过期，正在修改状态", lesson.getId());
                                boolean update = lessonService.lambdaUpdate()
                                        .set(LearningLesson::getStatus, LessonStatus.EXPIRED)
                                        .ne(LearningLesson::getStatus, LessonStatus.EXPIRED)
                                        .eq(LearningLesson::getId, lesson.getId())
                                        .update();
                                if (update)
                                    {
                                        log.info("课程{}状态修改成功，已修改为{}", lesson.getId(), lesson.getStatus());
                                    }
                                else
                                    {
                                        log.error("课程{}状态修改失败", lesson.getId());
                                    }
                            }
                    }
            }
    }
