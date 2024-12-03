package com.tianji.learning.mq;

import com.tianji.common.constants.MqConstants;
import com.tianji.learning.enums.PointsRecordType;
import com.tianji.learning.mq.message.SignInMessage;
import com.tianji.learning.service.IPointsRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author Encounter
 * @date 2024/12/02 22:35<br/>
 */
@Component
@RequiredArgsConstructor
public class LearningPointsListener
    {
        private final IPointsRecordService recordService;
        
        /**
         * listen write reply message （监听 编写回复消息）
         *
         * @param userId 用户id
         */
        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(name = "qa.points.queue", durable = "true"),
                exchange = @Exchange(name = MqConstants.Exchange.LEARNING_EXCHANGE, type = ExchangeTypes.TOPIC),
                key = MqConstants.Key.WRITE_REPLY
        ))
        public void listenWriteReplyMessage(Long userId)
            {
                recordService.addPointsRecord(userId, 5, PointsRecordType.QA);
            }
        
        /**
         * 监听登录消息
         *
         * @param message 消息
         */
        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(name = "sign.points.queue", durable = "true"),
                exchange = @Exchange(name = MqConstants.Exchange.LEARNING_EXCHANGE, type = ExchangeTypes.TOPIC),
                key = MqConstants.Key.SIGN_IN
        ))
        public void listenSignInMessage(SignInMessage message)
            {
                recordService.addPointsRecord(message.getUserId(), message.getPoints(), PointsRecordType.SIGN);
            }
        
        /**
         * 监听课程完成消息
         *
         * @param userId 用户id
         */
        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(name = "learning.points.queue", durable = "true"),
                exchange = @Exchange(name = MqConstants.Exchange.LEARNING_EXCHANGE, type = ExchangeTypes.TOPIC),
                key = MqConstants.Key.LEARN_SECTION
        ))
        public void listenLessonFinishMessage(Long userId)
            {
                recordService.addPointsRecord(userId, 10, PointsRecordType.LEARNING);
            }
    }
