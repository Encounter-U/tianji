package com.tianji.remark.mq;

import com.tianji.common.constants.MqConstants;
import com.tianji.remark.domain.po.LikedRecord;
import com.tianji.remark.service.ILikedRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author Encounter
 * @date 2024/11/30 19:08<br/>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikedRecordListener
    {
        private final ILikedRecordService likedRecordService;
        
        /**
         * 监听点赞记录变化--删除点赞记录
         *
         * @param bizId 业务id
         */
        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(value = "qa.liked.record.queue", durable = "true"),
                exchange = @Exchange(value = MqConstants.Exchange.LIKE_RECORD_EXCHANGE, type = ExchangeTypes.TOPIC),
                key = MqConstants.Key.QA_LIKED_RECORD_DELETE_KEY
        ))
        public void listenLikedRecordChange(Long bizId)
            {
                log.debug("监听到点赞记录发生变化");
                
                LikedRecord likedRecord = likedRecordService.lambdaQuery()
                        .eq(LikedRecord::getBizId, bizId)
                        .one();
                if (likedRecord == null)
                    {
                        log.debug("点赞记录不存在或尚未持久化");
                        return;
                    }
                
                //删除点赞记录
                likedRecordService.lambdaUpdate()
                        .eq(LikedRecord::getBizId, bizId)
                        .remove();
            }
    }
