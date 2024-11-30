package com.tianji.api.client.remark;

import com.tianji.api.client.remark.fallback.RemarkClientFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

/**
 * @author Encounter
 * @date 2024/11/29 15:21<br/>
 */
@FeignClient(value = "remark-service", fallbackFactory = RemarkClientFallBack.class)
public interface RemarkClient
    {
        @GetMapping("/likes/list")
        Set<Long> likeList(@RequestParam Iterable<Long> bizIds);
    }
