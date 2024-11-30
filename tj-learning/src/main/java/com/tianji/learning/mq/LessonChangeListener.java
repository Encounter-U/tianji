package com.tianji.learning.mq;

import com.tianji.api.dto.trade.OrderBasicDTO;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.utils.CollUtils;
import com.tianji.learning.service.ILearningLessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Encounter
 * @date 2024/11/16 23:06<br/>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LessonChangeListener
    {
        @Autowired
        private ILearningLessonService learningLessonService;
        
        /**
         * 监听订单支付或课程报名的消息
         *
         * @param order 订购基本 DTO
         */
        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(name = "learning.lesson.pay.queue", durable = "true"),
                exchange = @Exchange(name = MqConstants.Exchange.LEARNING_EXCHANGE, type = "topic"),
                key = "lesson-key"
        ))
        public void listenLessonPay(OrderBasicDTO order)
            {
                //健壮性处理
                if (order == null || order.getOrderId() == null || CollUtils.isEmpty(order.getCourseIds()))
                    {
                        log.error("接收到MQ消息有误，订单数据为空");
                        return;
                    }
                
                //添加课程
                log.debug("监听到用户{}的订单{}，需要添加课程{}到课表中",
                        order.getUserId(), order.getOrderId(), order.getCourseIds());
                learningLessonService.addUserLessons(order.getUserId(), order.getCourseIds());
            }
        
        /**
         * 监听订单退款成功删除课程
         *
         * @param order 订单
         */
        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(name = "learning.lesson.refund.queue", durable = "true"),
                exchange = @Exchange(name = MqConstants.Exchange.ORDER_EXCHANGE, type = "topic"),
                key = MqConstants.Key.ORDER_REFUND_KEY
        ))
        public void listenLessonDelete(OrderBasicDTO order)
            {
                //健壮性处理
                if (order == null || order.getOrderId() == null || CollUtils.isEmpty(order.getCourseIds()))
                    {
                        log.error("接收到MQ消息有误，订单数据为空");
                        return;
                    }
                
                //删除课程
                log.debug("监听到用户{}的订单{}，需要删除课程：{}",
                        order.getUserId(), order.getOrderId(), order.getCourseIds());
                learningLessonService.deleteUserLessons(order.getUserId(), order.getCourseIds());
                
            }
    }
