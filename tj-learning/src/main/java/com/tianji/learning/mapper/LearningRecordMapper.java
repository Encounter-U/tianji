package com.tianji.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.api.dto.IdAndNumDTO;
import com.tianji.learning.domain.po.LearningRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LearningRecordMapper extends BaseMapper<LearningRecord>
    {
        /**
         * 对学习节进行计数
         *
         * @param userId 用户id
         * @param start  开始
         * @param end    结束
         * @return {@link List }<{@link IdAndNumDTO }> 学习节计数
         */
        List<IdAndNumDTO> countLearnedSections(
                @Param("userId") Long userId,
                @Param("start") LocalDateTime start,
                @Param("end") LocalDateTime end);
    }