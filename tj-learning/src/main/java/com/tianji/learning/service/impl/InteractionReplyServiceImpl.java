package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.remark.RemarkClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.ReplyDTO;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.domain.query.ReplyPageQuery;
import com.tianji.learning.domain.vo.ReplyVO;
import com.tianji.learning.enums.QuestionStatus;
import com.tianji.learning.mapper.InteractionReplyMapper;
import com.tianji.learning.service.IInteractionQuestionService;
import com.tianji.learning.service.IInteractionReplyService;
import com.tianji.remark.constant.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.tianji.common.constants.Constant.DATA_FIELD_NAME_CREATE_TIME;
import static com.tianji.common.constants.Constant.DATA_FIELD_NAME_LIKED_TIME;

@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionReplyServiceImpl extends ServiceImpl<InteractionReplyMapper, InteractionReply> implements IInteractionReplyService
    {
        private final IInteractionQuestionService questionService;
        private final UserClient userClient;
        private final RemarkClient remarkClient;
        private final StringRedisTemplate redisTemplate;
        
        /**
         * 新增回答或评论
         *
         * @param replyDTO 回复 DTO
         */
        @Override
        @Transactional
        public void addReply(ReplyDTO replyDTO)
            {
                InteractionReply reply = BeanUtils.copyBean(replyDTO, InteractionReply.class);
                reply.setUserId(UserContext.getUser());
                save(reply);
                //有上级回答id，更新回答表的回复数
                if (reply.getAnswerId() != null)
                    {
                        String column = "reply_times";
                        lambdaUpdate()
                                .eq(InteractionReply::getId, reply.getAnswerId())
                                .setSql(StringUtils.isNotBlank(column), String.format("`%s` = `%s` + 1", column, column))
                                .update();
                    }
                //无论有没有上级回答id，均更新问题表的回答数
                String column = "answer_times";
                questionService.lambdaUpdate()
                        .eq(InteractionQuestion::getId, reply.getQuestionId())
                        .setSql(StringUtils.isNotBlank(column), String.format("`%s` = `%s` + 1", column, column))
                        .set(InteractionQuestion::getLatestAnswerId, reply.getId())
                        .set(replyDTO.getIsStudent(), InteractionQuestion::getStatus, QuestionStatus.UN_CHECK.getValue())
                        .update();
            }
        
        /**
         * 分页查询回答或评论
         *
         * @param query 查询条件
         * @return {@link PageDTO }<{@link ReplyVO }>
         */
        @Override
        public PageDTO<ReplyVO> pageReply(ReplyPageQuery query, boolean isAdmin)
            {
                //校验参数
                Long answerId = query.getAnswerId();
                Long questionId = query.getQuestionId();
                if (questionId == null && answerId == null)
                    {
                        throw new BadRequestException("问题id和回答id至少指定一个");
                    }
                
                //查询数据
                Page<InteractionReply> page = lambdaQuery()
                        .eq(questionId != null, InteractionReply::getQuestionId, questionId)
                        .eq(InteractionReply::getAnswerId, answerId == null ? 0L : answerId)
                        .eq(!isAdmin, InteractionReply::getHidden, false)
                        .page(query.toMpPage(
                                new OrderItem(DATA_FIELD_NAME_CREATE_TIME, false),
                                new OrderItem(DATA_FIELD_NAME_LIKED_TIME, true)));
                //.page(query.toMpPageDefaultSortByCreateTimeDesc());
                List<InteractionReply> records = page.getRecords();
                
                //没有数据直接返回
                if (CollUtils.isEmpty(records))
                    {
                        return PageDTO.empty(0L, 0L);
                    }
                
                //获取用户信息
                Set<Long> userIds = new HashSet<>();
                Set<Long> replyIds = new HashSet<>();
                //存入业务id
                Set<Long> bizIds = new HashSet<>();
                for (InteractionReply reply : records)
                    {
                        //管理员可以看到所有回复，非管理员只能看到非匿名回复
                        if (isAdmin || !reply.getAnonymity())
                            {
                                userIds.add(reply.getUserId());
                            }
                        //回复id添加
                        replyIds.add(reply.getTargetReplyId());
                        //业务id添加，只有评论和回答能点赞
                        bizIds.add(reply.getId());
                    }
                //查询用户信息并转为map
                Map<Long, UserDTO> userMap = new HashMap<>();
                if (CollUtils.isNotEmpty(userIds))
                    {
                        List<UserDTO> userDTOS = userClient.queryUserByIds(userIds);
                        userMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
                    }
                
                //查询回复的上级回答是否为匿名，若不是匿名则添加targetUserId到userIds
                // replyIds.remove(0L)是为了去除回答的上级回答id，因为回答的上级回答id是0
                replyIds.remove(0L);
                Map<Long, UserDTO> targetUserMap = new HashMap<>();
                if (CollUtils.isNotEmpty(replyIds))
                    {
                        Set<Long> targetUserIds = listByIds(replyIds).stream()
                                .filter(r -> !r.getAnonymity())
                                .map(InteractionReply::getTargetUserId)
                                .collect(Collectors.toSet());
                        //userIds.addAll(targetUserIds);
                        List<UserDTO> userDTOS = userClient.queryUserByIds(targetUserIds);
                        targetUserMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, u -> u));
                    }
                
                //查询点赞信息
                Set<Long> likeList = null;
                if (CollUtils.isNotEmpty(bizIds))
                    {
                        likeList = remarkClient.likeList(bizIds);
                    }
                
                //封装用户信息
                List<ReplyVO> replyVOList = new ArrayList<>();
                for (InteractionReply reply : records)
                    {
                        ReplyVO replyVO = BeanUtils.copyBean(reply, ReplyVO.class);
                        //回答用户信息
                        if (!reply.getAnonymity() || isAdmin)
                            {
                                UserDTO replyUser = userMap.get(reply.getUserId());
                                if (replyUser != null)
                                    {
                                        //回答用户存在且非匿名，设置回答用户信息
                                        replyVO.setUserName(replyUser.getName());
                                        replyVO.setUserIcon(replyUser.getIcon());
                                    }
                            }
                        //目标用户信息
                        if (reply.getTargetUserId() != null)
                            {
                                UserDTO targetUser = targetUserMap.get(reply.getTargetUserId());
                                if (targetUser != null)
                                    {
                                        //目标用户存在且非匿名，设置目标用户信息
                                        replyVO.setTargetUserName(targetUser.getName());
                                    }
                            }
                        
                        //先查redis看是否有点赞总量，没有再查数据库
                        Set<String> remark = redisTemplate.opsForSet().members(RedisConstants.LIKE_BIZ_KEY_PREFIX + reply.getId());
                        if (remark != null && !remark.isEmpty())
                            {
                                //点赞数
                                replyVO.setLikedTimes(remark.size());
                            }
                        
                        //判断当前用户是否点赞
                        if (likeList != null)
                            {
                                replyVO.setLiked(likeList.contains(reply.getId()));
                            }
                        
                        //添加到列表
                        replyVOList.add(replyVO);
                    }
                
                //封装数据并返回
                return PageDTO.of(page, replyVOList);
            }
        
        /**
         * 根据显示或隐藏回复
         *
         * @param id     ReplyId
         * @param hidden 是否隐藏
         */
        @Override
        public void hiddenReply(Long id, Boolean hidden)
            {
                //判断回复是否存在
                InteractionReply reply = getById(id);
                if (reply == null)
                    {
                        throw new BadRequestException("回复不存在");
                    }
                //隐藏回复
                lambdaUpdate()
                        .eq(InteractionReply::getId, id)
                        .set(InteractionReply::getHidden, hidden)
                        .update();
                
                //若隐藏的是回答，则回答下的所有评论也隐藏
                if (reply.getAnswerId() == null || reply.getAnswerId() == 0)
                    {
                        lambdaUpdate()
                                .eq(InteractionReply::getAnswerId, id)
                                .set(InteractionReply::getHidden, hidden)
                                .update();
                    }
            }
    }
