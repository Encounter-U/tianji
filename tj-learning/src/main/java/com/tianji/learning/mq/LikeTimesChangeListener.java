package com.tianji.learning.mq;

import com.tianji.api.dto.remark.LikedTimesDTO;
import com.tianji.common.constants.MqConstants;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.service.IInteractionReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Encounter
 * @date 2024/11/29 16:32<br/>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikeTimesChangeListener
    {
        private final IInteractionReplyService replyService;
        
        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(value = "qa.liked.times.queue", durable = "true"),
                exchange = @Exchange(value = MqConstants.Exchange.LIKE_RECORD_EXCHANGE, type = ExchangeTypes.TOPIC),
                key = MqConstants.Key.QA_LIKED_TIMES_KEY
        ))
        public void listenReplyLikedTimesChange(List<LikedTimesDTO> likedTimesDTOS)
            {
                log.debug("监听到回答或评论点赞数发生变化变化消息");
                
                //数据转换
                List<InteractionReply> replies = new ArrayList<>();
                for (LikedTimesDTO likedTimesDTO : likedTimesDTOS)
                    {
                        InteractionReply reply = new InteractionReply();
                        reply.setId(likedTimesDTO.getBizId());
                        reply.setLikedTimes(likedTimesDTO.getLikedTimes());
                        replies.add(reply);
                    }
                
                //批量更新
                replyService.updateBatchById(replies);
            }
    }
