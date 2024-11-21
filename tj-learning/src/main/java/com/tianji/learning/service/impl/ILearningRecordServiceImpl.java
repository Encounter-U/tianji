package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.api.dto.leanring.LearningRecordDTO;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.exceptions.DbException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.LearningRecordFormDTO;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.enums.SectionType;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
import com.tianji.learning.service.ILearningRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ILearningRecordServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord> implements ILearningRecordService
    {
        private final CourseClient courseClient;
        private final ILearningLessonService learningLessonService;
        
        /**
         * 按课程查询学习记录
         *
         * @param courseId 课程id
         * @return {@link LearningLessonDTO } 学习记录
         */
        @Override
        public LearningLessonDTO queryLearningRecordByCourse(Long courseId)
            {
                //判断课程是否存在
                CourseFullInfoDTO courseInfo = courseClient.getCourseInfoById(courseId, false, false);
                if (courseInfo == null)
                    {
                        return null;
                    }
                //获取当前用户
                Long userId = UserContext.getUser();
                //根据userId与courseId查询课表
                LearningLesson lesson = learningLessonService.selectOne(userId, courseId);
                if (lesson == null)
                    {
                        return null;
                    }
                //查询学习记录
                List<LearningRecord> learningRecords = lambdaQuery()
                        .eq(LearningRecord::getLessonId, lesson.getId())
                        .list();
                //封装返回结果
                LearningLessonDTO learningLessonDTO = new LearningLessonDTO();
                //课表id
                learningLessonDTO.setId(lesson.getId());
                //最近学习的小节id
                learningLessonDTO.setLatestSectionId(lesson.getLatestSectionId());
                //小节信息
                List<LearningRecordDTO> learningRecordDTOS = learningRecords.stream()
                        .map(learningRecord ->
                            {
                                LearningRecordDTO learningRecordDTO = new LearningRecordDTO();
                                learningRecordDTO.setSectionId(learningRecord.getSectionId());
                                learningRecordDTO.setFinished(learningRecord.getFinished());
                                learningRecordDTO.setMoment(learningRecord.getMoment());
                                return learningRecordDTO;
                            })
                        .collect(Collectors.toList());
                
                learningLessonDTO.setRecords(learningRecordDTOS);
                return learningLessonDTO;
            }
        
        /**
         * 添加学习记录
         *
         * @param formDTO 表格 DTO
         */
        @Override
        public void addLearningRecord(LearningRecordFormDTO formDTO)
            {
                // 获取登录用户
                Long userId = UserContext.getUser();
                
                // 处理学习记录，判断是否为第一次完成，默认未完成
                boolean finished;
                //当前小节类型
                if (formDTO.getSectionType() == SectionType.VIDEO)
                    {
                        finished = handleVideoRecord(userId, formDTO);
                    }
                else
                    {
                        finished = handleExamRecord(userId, formDTO);
                    }
                
                //更新学习记录
                //判断当前课表是否存在
                LearningLesson lesson = learningLessonService.getById(formDTO.getLessonId());
                if (lesson == null)
                    {
                        throw new BizIllegalException("课程不存在，无法更新数据！");
                    }
                //标记当前小节是否为最后一小节，默认不是
                boolean lastSection = false;
                //判断是否有新增小节
                if (finished)
                    {
                        //有，获取并判断当前小节是否存在
                        CourseFullInfoDTO courseInfo = courseClient.getCourseInfoById(lesson.getCourseId(), false, false);
                        if (courseInfo == null)
                            {
                                throw new BizIllegalException("课程不存在，无法更新数据！");
                            }
                        //获取当前小节并判断是否为最后一小节
                        lastSection = formDTO.getSectionId() >= courseInfo.getSectionNum();
                    }
                //否，更新已学习小节数量
                learningLessonService.lambdaUpdate()
                        .set(lesson.getLearnedSections() == 0, LearningLesson::getStatus, LessonStatus.LEARNING.getValue())
                        .set(!finished, LearningLesson::getLatestSectionId, formDTO.getSectionId())
                        .set(lastSection, LearningLesson::getStatus, LessonStatus.FINISHED.getValue())
                        .set(!finished, LearningLesson::getLatestLearnTime, formDTO.getCommitTime())
                        .setSql(finished, "learned_sections = learned_sections + 1")
                        .eq(LearningLesson::getId, lesson.getId())
                        .update();
            }
        
        /**
         * 处理考试记录，只要提交就算完成
         *
         * @param userId  用户id
         * @param formDTO 表格 DTO
         * @return boolean 是否完成
         */
        private boolean handleExamRecord(Long userId, LearningRecordFormDTO formDTO)
            {
                //判断记录是否已存在
                LearningRecord record = getOldRecord(userId, formDTO);
                //直接新增
                record.setUserId(userId);
                record.setFinished(true);
                record.setFinishTime(formDTO.getCommitTime());
                //添加失败
                if (!save(record))
                    {
                        throw new DbException("添加学习记录失败");
                    }
                return true;
            }
        
        /**
         * 处理视频是否完成，进度大于等于一半则算完成
         *
         * @param userId  用户id
         * @param formDTO 表格 DTO
         * @return boolean 是否完成
         */
        private boolean handleVideoRecord(Long userId, LearningRecordFormDTO formDTO)
            {
                //判断记录是否已存在
                LearningRecord oldRecord = getOldRecord(userId, formDTO);
                
                if (oldRecord == null)
                    {
                        //添加学习记录
                        oldRecord = BeanUtils.copyBean(formDTO, LearningRecord.class);
                        oldRecord.setUserId(userId);
                        //oldRecord.setCreateTime(LocalDateTime.now());
                        if (!save(oldRecord))
                            {
                                throw new DbException("添加学习记录失败");
                            }
                        return false;
                    }
                else
                    {
                        //存在
                        //判断是否为第一次完成(完成状态为false，且当前观看时长大于等于视频总时长的一半)
                        boolean finished = !oldRecord.getFinished() && formDTO.getMoment() * 2 >= formDTO.getDuration();
                        //修改学习记录
                        boolean success = lambdaUpdate()
                                .set(LearningRecord::getFinished, true)
                                .set(LearningRecord::getFinishTime, formDTO.getCommitTime())
                                .update();
                        //修改失败
                        if (!success)
                            {
                                throw new DbException("更新学习记录失败");
                            }
                        return finished;
                    }
                
            }
        
        /**
         * 获取旧记录
         *
         * @param userId  用户id
         * @param formDTO 表格 DTO
         * @return {@link LearningRecord } 旧记录
         */
        private LearningRecord getOldRecord(Long userId, LearningRecordFormDTO formDTO)
            {
                return lambdaQuery()
                        .eq(LearningRecord::getLessonId, formDTO.getLessonId())
                        .eq(LearningRecord::getSectionId, formDTO.getSectionId())
                        .eq(LearningRecord::getUserId, userId)
                        .one();
            }
    }
