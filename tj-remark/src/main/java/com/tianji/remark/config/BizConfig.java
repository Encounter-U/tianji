package com.tianji.remark.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Encounter
 * @date 2024/11/30 14:29<br/>
 */
@Data
@Component
@ConfigurationProperties(prefix = "tj.biz")
public class BizConfig
    {
        private List<String> bizTypes;
        private int maxBizSize;
    }
