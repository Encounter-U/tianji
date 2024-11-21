package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.dto.LearningPlanDTO;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.domain.vo.LearningPlanPageVO;
import com.tianji.learning.domain.vo.LessonStatusVO;
import com.tianji.learning.service.ILearningLessonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 学生课程表(learning_lesson)表控制层
 *
 * @author Encounter
 * @date 2024-11-16
 */
@Api(tags = "我的课程")
@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LearningLessonController
    {
        /**
         * 服务对象
         */
        private final ILearningLessonService lessonService;
        
        /**
         * 分页查询我的课程
         *
         * @param page 分页信息
         * @return {@link PageDTO }<{@link LearningLessonVO }> 单页信息
         */
        @GetMapping("/page")
        @ApiOperation("查询我的课表，排序字段 latest_learn_time:学习时间排序，create_time:购买时间排序")
        public PageDTO<LearningLessonVO> queryMyLesson(PageQuery page)
            {
                return lessonService.queryMyLesson(page);
            }
        
        /**
         * 查询我的当前课程
         *
         * @return {@link LearningLessonVO } 我正在学习的课程
         */
        @GetMapping("/now")
        @ApiOperation("查询我正在学习的课程")
        public LearningLessonVO queryMyCurrentLesson()
            {
                return lessonService.queryMyCurrentLesson();
            }
        
        /**
         * 验证课程是否有效
         *
         * @param courseId 课程id
         * @return {@link Long } 有效则返回课表id，无效则返回null
         */
        @GetMapping("/{courseId}/valid")
        @ApiOperation("验证课程是否有效")
        public Long isLessonValid(@PathVariable Long courseId)
            {
                return lessonService.isLessonValid(courseId);
            }
        
        /**
         * 获取课程状态
         *
         * @param courseId 课程id
         * @return {@link LessonStatusVO } 课程状态信息
         */
        @GetMapping("/{courseId}")
        @ApiOperation("查询课程状态")
        public LessonStatusVO getLessonStatus(@PathVariable Long courseId)
            {
                return lessonService.getLessonStatus(courseId);
            }
        
        /**
         * 统计课程学习人数
         *
         * @param courseId 课程id
         * @return {@link Integer } 学习人数
         */
        @GetMapping("/lessons/{courseId}/count")
        @ApiOperation("统计课程学习人数")
        public Integer countLearningLessonByCourse(@PathVariable("courseId") Long courseId)
            {
                return lessonService.countLearningLessonByCourse(courseId);
            }
        
        /**
         * 用户删除已失效课程课程
         *
         * @param courseId 课程id
         */
        @DeleteMapping("/delete/{courseId}")
        @ApiOperation("删除我的课程")
        public void deleteUserLessons(@PathVariable Long courseId)
            {
                lessonService.deleteExpiredLessons(courseId);
            }
        
        /**
         * 创建学习计划
         *
         * @param planDTO 计划 DTO
         */
        @ApiOperation("创建学习计划")
        @PostMapping("/plans")
        public void createLearningPlans(@Valid @RequestBody LearningPlanDTO planDTO)
            {
                lessonService.createLearningPlan(planDTO.getCourseId(), planDTO.getFreq());
            }
        
        @ApiOperation("查询我的学习计划")
        @GetMapping("/plans")
        public LearningPlanPageVO queryMyPlans(PageQuery query)
            {
                return lessonService.queryMyPlans(query);
            }
    }
