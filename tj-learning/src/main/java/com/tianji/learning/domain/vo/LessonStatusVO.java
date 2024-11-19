package com.tianji.learning.domain.vo;

import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.enums.PlanStatus;
import lombok.Data;

/**
 * @author Encounter
 * @date 2024/11/18 13:28<br/>
 * 课程状态信息
 */
@Data
public class LessonStatusVO
    {
        //主键lessonId
        private Long id;
        //课程id
        private Long courseId;
        //课程状态：0-未学习，1-学习中，2-已学完，3-已失效
        private LessonStatus status;
        //总已学习小节数
        private int learnedSections;
        //创建时间
        private String createTime;
        //过期时间
        private String expireTime;
        //学习计划状态：0-没有计划，1-计划进行中
        private PlanStatus planStatus;
    }
