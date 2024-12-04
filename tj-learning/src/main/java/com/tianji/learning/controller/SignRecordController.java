package com.tianji.learning.controller;

import com.tianji.learning.domain.vo.SignResultVO;
import com.tianji.learning.service.ISignRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Encounter
 * @date 2024/12/02 15:59<br/>
 */
@RequestMapping("/sign-records")
@RestController
@RequiredArgsConstructor
@Tag(name = "签到记录", description = "签到记录相关接口")
public class SignRecordController
    {
        private final ISignRecordService signRecordService;
        
        /**
         * 添加标志记录
         *
         * @return {@link SignResultVO }
         */
        @PostMapping
        @Operation(summary = "签到", description = "用户签到")
        public SignResultVO addSignRecord()
            {
                return signRecordService.addSignRecord();
            }
        
        /**
         * 查询签到记录
         *
         * @return {@link Byte[] }
         */
        @GetMapping
        @Operation(summary = "查询签到记录", description = "查询用户签到记录")
        public Byte[] querySignRecord()
            {
                return signRecordService.querySignRecord();
            }
    }
