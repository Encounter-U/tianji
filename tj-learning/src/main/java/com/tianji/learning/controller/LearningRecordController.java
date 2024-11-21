package com.tianji.learning.controller;

import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.learning.domain.dto.LearningRecordFormDTO;
import com.tianji.learning.service.ILearningRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author Encounter
 * @date 2024/11/19 18:49 <br>
 * <p>
 * 学习记录表(learning_record)表控制层
 */
@RestController
@RequestMapping("/learning_records")
@RequiredArgsConstructor
@Api(tags = "学习记录")
public class LearningRecordController
    {
        private final ILearningRecordService recordService;
        
        /**
         * 按课程查询学习记录
         *
         * @param courseId 课程id
         * @return {@link LearningLessonDTO } 学习记录
         */
        @GetMapping("/learning-records/course/{courseId}")
        public LearningLessonDTO queryLearningRecordByCourse(@PathVariable("courseId") Long courseId)
            {
                return recordService.queryLearningRecordByCourse(courseId);
            }
        
        /**
         * 添加学习记录
         *
         * @param formDTO 表格 DTO
         */
        @ApiOperation("提交学习记录")
        @PostMapping
        public void addLearningRecord(@RequestBody LearningRecordFormDTO formDTO)
            {
                recordService.addLearningRecord(formDTO);
            }
    }
