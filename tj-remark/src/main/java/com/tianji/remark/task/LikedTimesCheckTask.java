package com.tianji.remark.task;

import com.tianji.remark.service.ILikedRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Encounter
 * @date 2024/11/29 17:43<br/>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikedTimesCheckTask
    {
        private static final List<String> BIZ_TYPES = List.of("QA", "NOTE");
        private static final int MAX_BIZ_SIZE = 30;
        
        private final ILikedRecordService recordService;
        
        /**
         * 定时检查点赞次数 20s
         */
        @Scheduled(fixedDelay = 20000)
        public void checkLikedTimes()
            {
                //log.debug("开始检查点赞次数...");
                for (String bizType : BIZ_TYPES)
                    {
                        recordService.readLikedTimesAndSendMessage(bizType, MAX_BIZ_SIZE);
                    }
            }
    }
