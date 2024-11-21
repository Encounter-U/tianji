package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.course.CatalogueClient;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.IdAndNumDTO;
import com.tianji.api.dto.course.CataSimpleInfoDTO;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.*;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.domain.vo.LearningPlanPageVO;
import com.tianji.learning.domain.vo.LearningPlanVO;
import com.tianji.learning.domain.vo.LessonStatusVO;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.enums.PlanStatus;
import com.tianji.learning.mapper.LearningLessonMapper;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
// 生成一个包含常量、构造函数、@NonNull注解的字段的类
@RequiredArgsConstructor
// 关闭所有警告
@SuppressWarnings("ALL")
public class LearningLessonServiceImpl extends ServiceImpl<LearningLessonMapper, LearningLesson> implements ILearningLessonService
    {
        private final CourseClient courseClient;
        private final CatalogueClient catalogueClient;
        private final LearningLessonMapper lessonMapper;
        private final LearningRecordMapper recordMapper;
        
        /**
         * 添加用户课程
         *
         * @param userId    用户id
         * @param courseIds 课程 ID
         */
        @Override
        public void addUserLessons(Long userId, List<Long> courseIds)
            {
                //根据课程id查询课程简单信息
                List<CourseSimpleInfoDTO> simpleInfoList = courseClient.getSimpleInfoList(courseIds);
                if (CollUtils.isEmpty(simpleInfoList))
                    {
                        log.error("课程信息不存在，无法添加到课表");
                        return;
                    }
                
                //创建集合，用于存储LearningLesson数据
                List<LearningLesson> lessons = new ArrayList<>();
                //循环遍历，处理LearningLesson数据
                for (CourseSimpleInfoDTO courseSimpleInfo : simpleInfoList)
                    {
                        LearningLesson lesson = new LearningLesson();
                        //设置用户id
                        lesson.setUserId(userId);
                        //设置课程id
                        lesson.setCourseId(courseSimpleInfo.getId());
                        //设置课程创建时间
                        LocalDateTime now = LocalDateTime.now();
                        lesson.setCreateTime(now);
                        //设置课程到期时间
                        Integer validDuration = courseSimpleInfo.getValidDuration();
                        if (validDuration != null && validDuration > 0)
                            {
                                lesson.setExpireTime(now.plusDays(validDuration));
                            }
                        
                        //储存到集合中
                        lessons.add(lesson);
                    }
                
                //批量添加课程
                saveBatch(lessons);
            }
        
        /**
         * 分页查询我的课程
         *
         * @param page 分页信息
         * @return {@link PageDTO }<{@link LearningLessonVO }> 单页信息
         */
        @Override
        public PageDTO<LearningLessonVO> queryMyLesson(PageQuery page)
            {
                //先知道我是谁
                Long userId = UserContext.getUser();
                //查询我的课程
                // 封装查询条件并执行查询
                Page<LearningLesson> lessonPage = lambdaQuery()
                        .eq(LearningLesson::getUserId, userId)
                        .page(page.toMpPage("latest_learn_time", false));
                //取出查询结果
                List<LearningLesson> lessons = lessonPage.getRecords();
                
                //判断是否为空
                if (CollUtils.isEmpty(lessons))
                    {
                        return PageDTO.empty(lessonPage);
                    }
                
                //查询课程详情
                Map<Long, CourseSimpleInfoDTO> courseSimpleInfoMap = queryCourseSimpleInfoList(lessons);
                
                //转换为VO
                ArrayList<LearningLessonVO> vos = new ArrayList<>();
                //遍历
                for (LearningLesson lesson : lessons)
                    {
                        //创建并填充信息到VO对象
                        LearningLessonVO vo = BeanUtils.copyBean(lesson, LearningLessonVO.class);
                        //获取课程信息
                        CourseSimpleInfoDTO cInfo = courseSimpleInfoMap.get(lesson.getCourseId());
                        //填充课程信息
                        vo.setCourseId(lesson.getCourseId());
                        vo.setCourseName(cInfo.getName());
                        vo.setCourseCoverUrl(cInfo.getCoverUrl());
                        vo.setSections(cInfo.getSectionNum());
                        
                        //添加到集合中
                        vos.add(vo);
                    }
                
                //返回结果
                return PageDTO.of(lessonPage, vos);
            }
        
        /**
         * 查询我当前课程
         *
         * @return {@link LearningLessonVO } 我正在学习的课程
         */
        @Override
        public LearningLessonVO queryMyCurrentLesson()
            {
                //先知道我是谁
                Long userId = UserContext.getUser();
                //查询我的课程
                LearningLesson lesson = lambdaQuery()
                        .eq(LearningLesson::getUserId, userId)
                        .eq(LearningLesson::getStatus, LessonStatus.LEARNING.getValue())
                        .orderByDesc(LearningLesson::getLatestLearnTime)
                        .last("limit 1")
                        .one();
                
                //判断是否为空
                if (lesson == null)
                    {
                        return null;
                    }
                
                //查询课程详情
                LearningLessonVO vo = BeanUtils.copyBean(lesson, LearningLessonVO.class);
                //查询课程信息
                CourseFullInfoDTO courseInfo = courseClient.getCourseInfoById(lesson.getCourseId(), false, false);
                if (courseInfo == null)
                    {
                        throw new BadRequestException("课程信息不存在！");
                    }
                //填充课程信息
                vo.setCourseName(courseInfo.getName());
                vo.setCourseCoverUrl(courseInfo.getCoverUrl());
                vo.setSections(courseInfo.getSectionNum());
                
                //总已报名课程数
                vo.setCourseAmount(lambdaQuery().eq(LearningLesson::getUserId, userId).count());
                //最近一次学习的小节信息
                List<CataSimpleInfoDTO> cataSimpleInfos = catalogueClient.batchQueryCatalogue(CollUtils.singletonList(lesson.getLatestSectionId()));
                
                if (CollUtils.isNotEmpty(cataSimpleInfos))
                    {
                        CataSimpleInfoDTO cataSimpleInfo = cataSimpleInfos.get(0);
                        vo.setLatestSectionName(cataSimpleInfo.getName());
                        vo.setLatestSectionIndex(cataSimpleInfo.getCIndex());
                    }
                //返回结果
                return vo;
            }
        
        /**
         * 用户退款删除课程
         *
         * @param userId    用户id
         * @param courseIds 课程 ID
         */
        @Override
        public void deleteUserLessons(Long userId, List<Long> courseIds)
            {
                //判断课程是否存在
                List<CourseSimpleInfoDTO> cInfos = courseClient.getSimpleInfoList(courseIds);
                if (CollUtils.isEmpty(cInfos))
                    {
                        log.error("课程信息不存在，无法删除");
                        return;
                    }
                //根据用户id和课程id删除课程
                lambdaUpdate()
                        .eq(LearningLesson::getUserId, userId)
                        .in(LearningLesson::getCourseId, courseIds)
                        .remove();
            }
        
        /**
         * 课程是否有效
         *
         * @param courseId 课程id
         * @return {@link Long } 有效则返回课表id，无效则返回null
         */
        @Override
        public Long isLessonValid(Long courseId)
            {
                //先知道我是谁
                Long userId = UserContext.getUser();
                //查询课程是否存在
                LearningLesson lesson = lambdaQuery()
                        .eq(LearningLesson::getUserId, userId)
                        .eq(LearningLesson::getCourseId, courseId)
                        .one();
                Long lessonId = null;
                //判断是否为空及是否过期
                LocalDateTime expireTime = lesson.getExpireTime();
                if (lesson != null && expireTime != null && expireTime.isAfter(LocalDateTime.now()))
                    {
                        lessonId = lesson.getId();
                    }
                else
                    {
                        log.debug("课程已过期或不存在");
                    }
                return lessonId;
            }
        
        /**
         * 获取课程状态
         *
         * @param courseId 课程id
         * @return {@link LessonStatusVO } 课程状态信息
         */
        @Override
        public LessonStatusVO getLessonStatus(Long courseId)
            {
                //先知道我是谁
                Long userId = UserContext.getUser();
                //查询课程是否存在
                LearningLesson lesson = lambdaQuery()
                        .eq(LearningLesson::getUserId, userId)
                        .eq(LearningLesson::getCourseId, courseId)
                        .one();
                //判断是否为空
                if (lesson == null)
                    {
                        return null;
                    }
                //创建VO对象
                LessonStatusVO vo = BeanUtils.copyBean(lesson, LessonStatusVO.class);
                
                return vo;
            }
        
        /**
         * 统计课程学习人数
         *
         * @param courseId 课程id
         * @return {@link Integer } 学习人数
         */
        @Override
        public Integer countLearningLessonByCourse(Long courseId)
            {
                //查询课程学习人数
                return lambdaQuery()
                        .eq(LearningLesson::getCourseId, courseId)
                        .count();
            }
        
        /**
         * 删除过期课程
         *
         * @param courseId 课程id
         */
        @Override
        public void deleteExpiredLessons(Long courseId)
            {
                //查询课程是否存在
                CourseFullInfoDTO courseInfo = courseClient.getCourseInfoById(courseId, false, false);
                if (courseInfo == null)
                    {
                        throw new BadRequestException("课程信息不存在！");
                    }
                //查询课程是否过期
                LearningLesson lesson = lambdaQuery()
                        .eq(LearningLesson::getCourseId, courseId)
                        .eq(LearningLesson::getUserId, UserContext.getUser())
                        .one();
                if (lesson == null)
                    {
                        throw new BadRequestException("课程信息不存在！");
                    }
                //判断课程是否过期
                if (lesson.getExpireTime().isBefore(LocalDateTime.now()))
                    {
                        //已过期，删除课程
                        lambdaUpdate()
                                .eq(LearningLesson::getCourseId, courseId)
                                .eq(LearningLesson::getUserId, UserContext.getUser())
                                .remove();
                    }
                else
                    {
                        //课程不存在或未过期
                        log.debug("课程未过期，无法删除");
                        throw new BadRequestException("课程未过期，无法删除");
                    }
            }
        
        /**
         * 根据用户id和课程id查询选中的课程
         *
         * @param userId   用户id
         * @param courseId 课程id
         * @return {@link LearningLesson } 选中的课程
         */
        @Override
        public LearningLesson selectOne(Long userId, Long courseId)
            {
                //查询课程
                return lambdaQuery()
                        .eq(LearningLesson::getUserId, userId)
                        .eq(LearningLesson::getCourseId, courseId)
                        .one();
            }
        
        /**
         * 创建学习计划
         *
         * @param courseId 课程id
         * @param freq     频率
         */
        @Override
        public void createLearningPlan(Long courseId, Integer freq)
            {
                //判断课程是否存在
                LearningLesson lesson = selectOne(UserContext.getUser(), courseId);
                AssertUtils.isNotNull(lesson, "课程不存在！");
                //更新课程学习频率
                lesson.setWeekFreq(freq.byteValue());
                if (lesson.getPlanStatus() == PlanStatus.NO_PLAN)
                    {
                        lesson.setPlanStatus(PlanStatus.PLAN_RUNNING);
                    }
                updateById(lesson);
            }
        
        /**
         * 分页查询我计划
         *
         * @param query 查询
         * @return {@link LearningPlanPageVO } 我的学习计划
         */
        @Override
        public LearningPlanPageVO queryMyPlans(PageQuery query)
            {
                LearningPlanPageVO vo = new LearningPlanPageVO();
                //先知道我是谁
                Long userId = UserContext.getUser();
                //获取本周起始及结束时间
                LocalDate now = LocalDate.now();
                LocalDateTime start = DateUtils.getWeekBeginTime(now);
                LocalDateTime end = DateUtils.getWeekEndTime(now);
                //查询本周已学习的总的小节数量
                Integer weekFinished = recordMapper.selectCount(new LambdaQueryWrapper<LearningRecord>()
                        .eq(LearningRecord::getUserId, userId)
                        .eq(LearningRecord::getFinished, true)
                        .ge(LearningRecord::getUpdateTime, start)
                        .le(LearningRecord::getUpdateTime, end));
                vo.setWeekFinished(weekFinished);
                //查询本周计划的总的小节数量
                Integer weekTotalPlan = getBaseMapper().queryTotalPlan(userId);
                vo.setWeekTotalPlan(weekTotalPlan);
                
                //TODO Encounter 2024/11/21 15:55 本周学习积分
                
                //查询分页数据
                Page<LearningLesson> lessonPage = lambdaQuery()
                        .eq(LearningLesson::getUserId, userId)
                        .eq(LearningLesson::getStatus, LessonStatus.LEARNING.getValue())
                        .eq(LearningLesson::getPlanStatus, PlanStatus.PLAN_RUNNING.getValue())
                        .page(query.toMpPage("latest_learn_time", false));
                //判断是否为空
                List<LearningLesson> records = lessonPage.getRecords();
                if (CollUtils.isEmpty(records))
                    {
                        return vo.emptyPage(lessonPage);
                    }
                
                //查询课程信息
                Map<Long, CourseSimpleInfoDTO> courseSimpleInfoMap = queryCourseSimpleInfoList(records);
                //查询每个课程本周已学习的小节数量
                List<IdAndNumDTO> countList = recordMapper.countLearnedSections(userId, start, end);
                //转换为map
                Map<Long, Integer> countMap = IdAndNumDTO.toMap(countList);
                //转换为VO
                List<LearningPlanVO> vos = records.stream()
                        .map(lesson ->
                            {
                                LearningPlanVO lessonVO = BeanUtils.copyBean(lesson, LearningPlanVO.class);
                                //获取课程信息
                                CourseSimpleInfoDTO cInfo = courseSimpleInfoMap.get(lesson.getCourseId());
                                if (cInfo != null)
                                    {
                                        //填充课程信息
                                        lessonVO.setCourseName(cInfo.getName());
                                        lessonVO.setSections(cInfo.getSectionNum());
                                    }
                                
                                //设置每个课程的本周已学习数量
                                lessonVO.setWeekLearnedSections(countMap.getOrDefault(lesson.getCourseId(), 0));
                                return lessonVO;
                            })
                        .collect(Collectors.toList());
                
                //返回结果
                return vo.pageInfo(lessonPage.getTotal(), lessonPage.getPages(), vos);
            }
        
        /**
         * 根据我的课表信息查询课程简单信息
         *
         * @param lessons 我的课表
         * @return {@link Map }<{@link Long },{@link CourseSimpleInfoDTO }> 将课程简单信息转换为map
         */
        private Map<Long, CourseSimpleInfoDTO> queryCourseSimpleInfoList(List<LearningLesson> lessons)
            {
                //获取课程id
                Set<Long> courseIds = lessons.stream().map(LearningLesson::getCourseId).collect(Collectors.toSet());
                //查询课程简单信息
                List<CourseSimpleInfoDTO> simpleInfoList = courseClient.getSimpleInfoList(courseIds);
                //判断是否为空
                if (CollUtils.isEmpty(simpleInfoList))
                    {
                        //抛出异常
                        throw new BadRequestException("课程信息不存在！");
                    }
                //不为空，转换为map
                //key:课程id value:课程简单信息
                return simpleInfoList
                        .stream()
                        .collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));
            }
    }
