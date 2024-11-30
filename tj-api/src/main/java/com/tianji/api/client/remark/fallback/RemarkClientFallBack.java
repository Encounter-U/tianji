package com.tianji.api.client.remark.fallback;

import com.tianji.api.client.remark.RemarkClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collections;

/**
 * @author Encounter
 * @date 2024/11/29 15:22<br/>
 */
@Slf4j
public class RemarkClientFallBack implements FallbackFactory<RemarkClient>
    {
        /**
         * 该方法是fallback的工厂方法，当调用服务失败时，会调用该方法
         *
         * @param cause 异常
         * @return {@link RemarkClient }
         */
        @Override
        public RemarkClient create(Throwable cause)
            {
                log.error("查询remark-service服务异常", cause);
                
                return bizIds -> Collections.emptySet();
            }
    }
