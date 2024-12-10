package com.tianji.promotion.config;

import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Encounter
 * @date 2024/12/10 20:14<br/>
 */
@Configuration
@RequiredArgsConstructor
public class RabbitMqHelperConfig
    {
        private final RabbitTemplate rabbitTemplate;
        
        @Bean
        public RabbitMqHelper rabbitMqHelper()
            {
                return new RabbitMqHelper(rabbitTemplate);
            }
    }
