package com.tianji.learning.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.cache.CategoryCache;
import com.tianji.api.client.course.CatalogueClient;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.client.search.SearchClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.course.CataSimpleInfoDTO;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.domain.query.QuestionAdminPageQuery;
import com.tianji.learning.domain.query.QuestionPageQuery;
import com.tianji.learning.domain.vo.QuestionAdminVO;
import com.tianji.learning.domain.vo.QuestionVO;
import com.tianji.learning.enums.QuestionStatus;
import com.tianji.learning.mapper.InteractionQuestionMapper;
import com.tianji.learning.mapper.InteractionReplyMapper;
import com.tianji.learning.service.IInteractionQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionQuestionServiceImpl extends ServiceImpl<InteractionQuestionMapper, InteractionQuestion> implements IInteractionQuestionService
    {
        private final UserClient userClient;
        private final InteractionReplyMapper replyMapper;
        private final SearchClient searchClient;
        private final CourseClient courseClient;
        private final CatalogueClient catalogueClient;
        private final CategoryCache categoryCache;
        private final RabbitMqHelper mqHelper;
        
        /**
         * 保存问题
         *
         * @param questionFormDTO 问题表 DTO
         */
        @Override
        public void saveQuestion(QuestionFormDTO questionFormDTO)
            {
                //获取当前登录用户
                Long userId = UserContext.getUser();
                
                InteractionQuestion question = BeanUtil.copyProperties(questionFormDTO, InteractionQuestion.class);
                question.setUserId(userId);
                
                //保存问题
                save(question);
                
                //增加积分
                mqHelper.send(MqConstants.Exchange.LEARNING_EXCHANGE,
                        MqConstants.Key.WRITE_REPLY,
                        userId);
            }
        
        /**
         * 更新问题
         *
         * @param questionFormDTO 问题表 DTO
         * @param id              id
         */
        @Override
        public void updateQuestion(QuestionFormDTO questionFormDTO, Long id)
            {
                //验证数据是否存在
                InteractionQuestion q = getById(id);
                if (q == null)
                    {
                        throw new BadRequestException("问题不存在");
                    }
                
                //验证数据是否属于当前用户
                Long userId = UserContext.getUser();
                if (!userId.equals(q.getUserId()))
                    {
                        throw new BadRequestException("无权限操作");
                    }
                
                //封装数据
                InteractionQuestion question = BeanUtil.copyProperties(questionFormDTO, InteractionQuestion.class);
                question.setId(id);
                
                //更新问题
                updateById(question);
            }
        
        /**
         * 分页查询问题
         *
         * @param questionPageQuery 问题页面查询
         * @return {@link PageDTO }<{@link QuestionVO }>
         */
        @Override
        public PageDTO<QuestionVO> pageQuestion(QuestionPageQuery questionPageQuery)
            {
                //获取当前登录id
                Long userId = UserContext.getUser();
                Long courseId = questionPageQuery.getCourseId();
                Long sectionId = questionPageQuery.getSectionId();
                if (courseId == null && sectionId == null)
                    {
                        throw new BadRequestException("课程id和小节id不能同时为空");
                    }
                
                //分页查询所有未被隐藏的问题且根据创建时间倒序排序
                Page<InteractionQuestion> page = lambdaQuery()
                        .eq(InteractionQuestion::getHidden, false)
                        .eq(courseId != null, InteractionQuestion::getCourseId, courseId)
                        .eq(sectionId != null, InteractionQuestion::getSectionId, sectionId)
                        .eq(questionPageQuery.getOnlyMine(), InteractionQuestion::getUserId, userId)
                        .select(InteractionQuestion.class, i -> !i.getProperty().equals("description"))
                        .page(questionPageQuery.toMpPageDefaultSortByCreateTimeDesc());
                
                //没有数据直接返回
                if (CollUtils.isEmpty(page.getRecords()))
                    {
                        return PageDTO.empty(page);
                    }
                
                //封装用户以及回复id
                Set<Long> userIds = new HashSet<>();
                Set<Long> answerIds = new HashSet<>();
                for (InteractionQuestion question : page.getRecords())
                    {
                        //只有非匿名提问才查询用户信息
                        if (!question.getAnonymity())
                            userIds.add(question.getUserId());
                        //记录最新回复id
                        answerIds.add(question.getLatestAnswerId());
                    }
                
                //批量查询最新回复并封装为map key为问题id value为最新回复
                List<InteractionReply> replies = replyMapper.selectBatchIds(answerIds);
                Map<Long, InteractionReply> replyMap = new HashMap<>();
                for (InteractionReply reply : replies)
                    {
                        replyMap.put(reply.getQuestionId(), reply);
                        if (!reply.getHidden() && !reply.getAnonymity())
                            {
                                userIds.add(reply.getUserId());
                            }
                    }
                
                //批量查询用户信息并封装为map key为用户id value为用户信息
                List<UserDTO> userDTOS = userClient.queryUserByIds(userIds);
                Map<Long, UserDTO> userMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
                
                //遍历封装返回结果
                PageDTO<QuestionVO> result = PageDTO.of(page, QuestionVO.class);
                for (QuestionVO questionVO : result.getList())
                    {
                        //封装提问者信息
                        if (!questionVO.getAnonymity())
                            {
                                //非匿名提问
                                UserDTO userDTO = userMap.get(questionVO.getUserId());
                                questionVO.setUserName(userDTO.getName());
                                questionVO.setUserIcon(userDTO.getIcon());
                            }
                        //获取最近一次回复内容
                        InteractionReply reply = replyMap.get(questionVO.getId());
                        if (reply == null || reply.getReplyTimes() == 0 || reply.getHidden())
                            {
                                //没有回复或者回复被隐藏
                                //跳过后续逻辑 直接进行下一次循环
                                continue;
                            }
                        //回复次数
                        questionVO.setAnswerTimes(reply.getReplyTimes());
                        //回复内容
                        questionVO.setLatestReplyContent(reply.getContent());
                        if (!reply.getAnonymity())
                            //回复者昵称
                            questionVO.setLatestReplyUser(userMap.get(reply.getUserId()).getName());
                    }
                return result;
            }
        
        /**
         * 根据id获取问题详情
         *
         * @param id 问题id
         * @return {@link QuestionVO } 问题详情
         */
        @Override
        public QuestionVO getQuestionById(Long id)
            {
                InteractionQuestion interactionQuestion = getById(id);
                if (interactionQuestion == null || interactionQuestion.getHidden())
                    {
                        //问题不存在或者被隐藏
                        return null;
                    }
                QuestionVO questionVO = BeanUtil.copyProperties(interactionQuestion, QuestionVO.class);
                if (!questionVO.getAnonymity())
                    {
                        //非匿名提问
                        //获取提问者信息
                        UserDTO userDTO = userClient.queryUserById(questionVO.getUserId());
                        questionVO.setUserName(userDTO.getName());
                        questionVO.setUserIcon(userDTO.getIcon());
                    }
                return questionVO;
            }
        
        /**
         * 删除由id
         *
         * @param id id
         */
        @Override
        @Transactional
        public void deleteById(Long id)
            {
                InteractionQuestion question = getById(id);
                if (question == null)
                    {
                        throw new BadRequestException("问题不存在");
                    }
                //验证数据是否属于当前用户
                Long userId = UserContext.getUser();
                if (!userId.equals(question.getUserId()))
                    {
                        throw new BadRequestException("无权限操作");
                    }
                //删除问题
                boolean questionFlag = removeById(id);
                //删除问题的回答及评论
                replyMapper.delete(new LambdaQueryWrapper<InteractionReply>().eq(InteractionReply::getQuestionId, id));
                if (!questionFlag)
                    {
                        throw new BadRequestException("删除失败");
                    }
            }
        
        /**
         * 管理员分页查询Question
         *
         * @param adminPageQuery 管理员分页查询条件
         * @return {@link PageDTO }<{@link QuestionAdminVO }> 问题列表
         */
        @Override
        public PageDTO<QuestionAdminVO> adminPage(QuestionAdminPageQuery adminPageQuery)
            {
                //查询课程是否存在
                List<Long> coursesIds = null;
                //检查字符串是否为空或者空白字符
                if (StringUtils.isNotBlank(adminPageQuery.getCourseName()))
                    {
                        coursesIds = searchClient.queryCoursesIdByName(adminPageQuery.getCourseName());
                        if (CollUtils.isEmpty(coursesIds))
                            {
                                log.error("课程不存在");
                                return PageDTO.empty(0L, 0L);
                            }
                    }
                //分页查询所有问题
                Page<InteractionQuestion> page = lambdaQuery()
                        .in(coursesIds != null, InteractionQuestion::getCourseId, coursesIds)
                        .eq(adminPageQuery.getStatus() != null, InteractionQuestion::getStatus, adminPageQuery.getStatus())
                        .in(CollUtils.isNotEmpty(coursesIds), InteractionQuestion::getCourseId, coursesIds)
                        .ge(adminPageQuery.getBeginTime() != null, InteractionQuestion::getCreateTime, adminPageQuery.getBeginTime())
                        .le(adminPageQuery.getEndTime() != null, InteractionQuestion::getCreateTime, adminPageQuery.getEndTime())
                        .page(adminPageQuery.toMpPageDefaultSortByCreateTimeDesc());
                
                //没有数据直接返回
                if (CollUtils.isEmpty(page.getRecords()))
                    {
                        return PageDTO.empty(page);
                    }
                
                //获取用户id，课程id，章节id
                Set<Long> userIds = new HashSet<>();
                Set<Long> courseIds = new HashSet<>();
                Set<Long> chapterIds = new HashSet<>();
                for (InteractionQuestion question : page.getRecords())
                    {
                        userIds.add(question.getUserId());
                        courseIds.add(question.getCourseId());
                        chapterIds.add(question.getChapterId());
                        chapterIds.add(question.getSectionId());
                    }
                
                //批量查询用户信息
                List<UserDTO> userDTOS = userClient.queryUserByIds(userIds);
                Map<Long, UserDTO> userMap = new HashMap<>();
                if (CollUtils.isNotEmpty(userDTOS))
                    {
                        userMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
                    }
                
                //批量查询课程信息
                List<CourseSimpleInfoDTO> simpleInfoList = courseClient.getSimpleInfoList(courseIds);
                Map<Long, CourseSimpleInfoDTO> courseMap = new HashMap<>();
                if (CollUtils.isNotEmpty(simpleInfoList))
                    {
                        courseMap = simpleInfoList.stream()
                                .collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));
                    }
                
                //批量查询章节信息
                List<CataSimpleInfoDTO> cataSimpleInfoDTOS = catalogueClient.batchQueryCatalogue(chapterIds);
                Map<Long, String> chapterMap = new HashMap<>();
                if (CollUtils.isNotEmpty(cataSimpleInfoDTOS))
                    {
                        chapterMap = cataSimpleInfoDTOS.stream()
                                .collect(Collectors.toMap(CataSimpleInfoDTO::getId, CataSimpleInfoDTO::getName));
                    }
                
                //转换数据
                List<QuestionAdminVO> voList = new ArrayList<>();
                for (InteractionQuestion question : page.getRecords())
                    {
                        QuestionAdminVO questionAdminVO = BeanUtils.copyBean(question, QuestionAdminVO.class);
                        //用户信息
                        UserDTO userDTO = userMap.get(question.getUserId());
                        if (userDTO != null)
                            {
                                questionAdminVO.setUserName(userDTO.getName());
                            }
                        //课程信息
                        CourseSimpleInfoDTO courseSimpleInfoDTO = courseMap.get(question.getCourseId());
                        if (courseSimpleInfoDTO != null)
                            {
                                questionAdminVO.setCourseName(courseSimpleInfoDTO.getName());
                                questionAdminVO.setCategoryName(categoryCache.getCategoryNames(courseSimpleInfoDTO.getCategoryIds()));
                            }
                        //章节信息
                        questionAdminVO.setChapterName(chapterMap.get(question.getChapterId()));
                        questionAdminVO.setSectionName(chapterMap.get(question.getSectionId()));
                        
                        //添加到返回结果
                        voList.add(questionAdminVO);
                    }
                
                return PageDTO.of(page, voList);
            }
        
        /**
         * 管理端隐藏或显示问题
         *
         * @param id     问题id
         * @param hidden 隐藏
         */
        @Override
        public void hidden(Long id, Boolean hidden)
            {
                //判断问题是否存在
                InteractionQuestion question = getById(id);
                if (question == null)
                    {
                        throw new BadRequestException("问题不存在");
                    }
                //隐藏问题
                question.setHidden(hidden);
                boolean flag = updateById(question);
                if (!flag)
                    {
                        throw new BadRequestException("操作失败");
                    }
            }
        
        /**
         * 管理获取由id
         *
         * @param id 问题id
         * @return {@link QuestionAdminVO }
         */
        @Override
        public QuestionAdminVO adminGetById(Long id)
            {
                InteractionQuestion question = getById(id);
                QuestionAdminVO vo = BeanUtils.copyBean(question, QuestionAdminVO.class);
                if (vo == null)
                    {
                        return null;
                    }
                //获取用户信息
                UserDTO userDTO = userClient.queryUserById(question.getUserId());
                if (userDTO != null)
                    {
                        vo.setUserName(userDTO.getName());
                        vo.setUserIcon(userDTO.getIcon());
                    }
                //获取课程信息
                CourseFullInfoDTO courseInfo = courseClient.getCourseInfoById(question.getCourseId(), false, true);
                if (courseInfo != null)
                    {
                        vo.setCourseName(courseInfo.getName());
                        //获取章节信息
                        List<CataSimpleInfoDTO> cataSimpleInfoDTOS = catalogueClient.batchQueryCatalogue(Collections.singleton(question.getChapterId()));
                        if (CollUtils.isNotEmpty(cataSimpleInfoDTOS))
                            vo.setChapterName(cataSimpleInfoDTOS.get(0).getName());
                        //获取三级分类信息
                        vo.setCategoryName(categoryCache.getCategoryNames(courseInfo.getCategoryIds()));
                        
                        //课程负责老师
                        List<UserDTO> userDTOS = userClient.queryUserByIds(courseInfo.getTeacherIds());
                        vo.setTeacherName(userDTOS.get(0).getName());
                    }
                
                //修改查看状态
                if (vo.getStatus() == QuestionStatus.UN_CHECK.getValue())
                    {
                        lambdaUpdate()
                                .set(InteractionQuestion::getStatus, QuestionStatus.CHECKED)
                                .eq(InteractionQuestion::getId, id)
                                .update();
                        vo.setStatus(QuestionStatus.CHECKED.getValue());
                    }
                
                //封装返回结果
                return vo;
            }
    }
