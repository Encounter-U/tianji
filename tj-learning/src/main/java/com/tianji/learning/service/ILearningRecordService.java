package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.learning.domain.dto.LearningRecordFormDTO;
import com.tianji.learning.domain.po.LearningRecord;

/**
 * @author Encounter
 * @date 2024/11/19 18:47 <br>
 */
public interface ILearningRecordService extends IService<LearningRecord>
    {
        
        
        /**
         * 按课程查询学习记录
         *
         * @param courseId 课程id
         * @return {@link LearningLessonDTO } 学习记录
         */
        LearningLessonDTO queryLearningRecordByCourse(Long courseId);
        
        /**
         * 添加学习记录
         *
         * @param formDTO 表格 DTO
         */
        void addLearningRecord(LearningRecordFormDTO formDTO);
    }
