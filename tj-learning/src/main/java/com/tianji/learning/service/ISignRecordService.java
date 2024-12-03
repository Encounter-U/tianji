package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.learning.domain.po.SignRecord;
import com.tianji.learning.domain.vo.SignResultVO;

/**
 * @author Encounter
 * @date 2024/12/02 16:03 <br>
 */
public interface ISignRecordService extends IService<SignRecord>
    {
        /**
         * 添加签到记录
         *
         * @return {@link SignResultVO }
         */
        SignResultVO addSignRecord();
        
        /**
         * 查询签到记录
         *
         * @return {@link Byte[] }
         */
        Byte[] querySignRecord();
    }
