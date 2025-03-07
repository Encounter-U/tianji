package com.tianji.remark.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.remark.domain.po.LikedRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Encounter
 * @date 2024/11/29 12:15 <br>
 */
@Mapper
public interface LikedRecordMapper extends BaseMapper<LikedRecord>
    {
        /**
         * 如果不存在，则插入批处理
         *
         * @param records 记录
         * @return boolean
         */
        boolean insertBatchIfNotExist(List<LikedRecord> records);
    }