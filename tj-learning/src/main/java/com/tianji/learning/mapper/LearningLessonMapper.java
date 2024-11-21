package com.tianji.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.learning.domain.po.LearningLesson;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LearningLessonMapper extends BaseMapper<LearningLesson>
    {
        /**
         * 查询总计划数量
         *
         * @param userId 用户id
         * @return {@link Integer } 总计划数量
         */
        Integer queryTotalPlan(Long userId);
    }