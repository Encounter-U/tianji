package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.enums.PlanStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author Encounter
 * @date 2024/11/16 23:03 <br>
 * 学生课程表
 */
@Data
@TableName("learning_lesson")
@Accessors(chain = true)
public class LearningLesson
    {
        /**
         * 主键
         */
        @TableId(value = "id", type = IdType.ASSIGN_ID)
        private Long id;
        
        /**
         * 学员id
         */
        private Long userId;
        
        /**
         * 课程id
         */
        private Long courseId;
        
        /**
         * 课程状态，0-未学习，1-学习中，2-已学完，3-已失效
         */
        private LessonStatus status;
        
        /**
         * 每周学习频率，例如每周学习6小节，则频率为6
         */
        private Byte weekFreq;
        
        /**
         * 学习计划状态，0-没有计划，1-计划进行中
         */
        private PlanStatus planStatus;
        
        /**
         * 已学习小节数量
         */
        private Integer learnedSections;
        
        /**
         * 最近一次学习的小节id
         */
        private Long latestSectionId;
        
        /**
         * 最近一次学习的时间
         */
        private LocalDateTime latestLearnTime;
        
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        
        /**
         * 过期时间
         */
        private LocalDateTime expireTime;
        
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }