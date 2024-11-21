package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.domain.vo.LearningPlanPageVO;
import com.tianji.learning.domain.vo.LessonStatusVO;

import java.util.List;

public interface ILearningLessonService extends IService<LearningLesson>
    {
        /**
         * 添加用户课程
         *
         * @param userId    用户id
         * @param courseIds 课程 ID
         */
        void addUserLessons(Long userId, List<Long> courseIds);
        
        /**
         * 分页查询我的课程
         *
         * @param page 分页信息
         * @return {@link PageDTO }<{@link LearningLessonVO }> 单页信息
         */
        PageDTO<LearningLessonVO> queryMyLesson(PageQuery page);
        
        /**
         * 查询我当前课程
         *
         * @return {@link LearningLessonVO } 我正在学习的课程
         */
        LearningLessonVO queryMyCurrentLesson();
        
        /**
         * 删除用户课程
         *
         * @param userId    用户id
         * @param courseIds 课程 ID
         */
        void deleteUserLessons(Long userId, List<Long> courseIds);
        
        /**
         * 验证课程是否有效
         *
         * @param courseId 课程id
         * @return {@link Long } 有效则返回课表id，无效则返回null
         */
        Long isLessonValid(Long courseId);
        
        /**
         * 获取课程状态
         *
         * @param courseId 课程id
         * @return {@link LessonStatusVO } 课程状态
         */
        LessonStatusVO getLessonStatus(Long courseId);
        
        /**
         * 统计课程学习人数
         *
         * @param courseId 课程id
         * @return {@link Integer } 学习人数
         */
        Integer countLearningLessonByCourse(Long courseId);
        
        /**
         * 删除过期课程
         *
         * @param courseId 课程id
         */
        void deleteExpiredLessons(Long courseId);
        
        /**
         * 根据用户id和课程id查询选中的课程
         *
         * @param userId   用户id
         * @param courseId 课程id
         * @return {@link LearningLesson } 选中的课程
         */
        LearningLesson selectOne(Long userId, Long courseId);
        
        /**
         * 创建学习计划
         *
         * @param courseId 课程id
         * @param freq     频率
         */
        void createLearningPlan(Long courseId, Integer freq);
        
        /**
         * 分页查询我计划
         *
         * @param query 查询
         * @return {@link LearningPlanPageVO }
         */
        LearningPlanPageVO queryMyPlans(PageQuery query);
    }
