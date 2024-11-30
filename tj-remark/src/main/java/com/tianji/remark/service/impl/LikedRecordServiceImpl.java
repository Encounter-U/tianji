//package com.tianji.remark.service.impl;
//
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
//import com.tianji.common.constants.MqConstants;
//import com.tianji.common.utils.BeanUtils;
//import com.tianji.common.utils.StringUtils;
//import com.tianji.common.utils.UserContext;
//import com.tianji.remark.domain.dto.LikeRecordFormDTO;
//import com.tianji.remark.domain.dto.LikedTimesDTO;
//import com.tianji.remark.domain.po.LikedRecord;
//import com.tianji.remark.mapper.LikedRecordMapper;
//import com.tianji.remark.service.ILikedRecordService;
//import lombok.RequiredArgsConstructor;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
/// **
// * @author Encounter
// * @date 2024/11/29 12:15 <br>
// */
////@Service
//@RequiredArgsConstructor
//public class LikedRecordServiceImpl extends ServiceImpl<LikedRecordMapper, LikedRecord> implements ILikedRecordService
//    {
//        private final RabbitMqHelper mqHelper;
//
//        /**
//         * 添加 Like Record
//         *
//         * @param likeRecordFormDTO LIKE 记录表 DTO
//         */
//        @Override
//        public void addLikeRecord(LikeRecordFormDTO likeRecordFormDTO)
//            {
//                //先判断当前要执行的操作是点赞还是取消点赞
//                boolean flag = likeRecordFormDTO.getLiked()
//                        ? liked(likeRecordFormDTO)
//                        : unlike(likeRecordFormDTO);
//
//                //是否执行成功
//                if (!flag)
//                    {
//                        log.error("点赞业务执行失败");
//                        return;
//                    }
//
//                //执行成功，统计点赞数
//                Integer count = lambdaQuery()
//                        .eq(LikedRecord::getBizId, likeRecordFormDTO.getBizId())
//                        .eq(LikedRecord::getBizType, likeRecordFormDTO.getBizType())
//                        .count();
//
//                //发送mq消息
//                mqHelper.send(MqConstants.Exchange.LIKE_RECORD_EXCHANGE,
//                        StringUtils.format(MqConstants.Key.LIKED_TIMES_KEY_TEMPLATE, likeRecordFormDTO.getBizType()),
//                        new LikedTimesDTO(likeRecordFormDTO.getBizId(), count));
//            }
//
//        /**
//         * 批量查询点赞列表
//         *
//         * @param bizIds 业务 ID
//         * @return {@link Set }<{@link Long }>
//         */
//        @Override
//        public Set<Long> likedList(List<Long> bizIds)
//            {
//                if (bizIds.isEmpty())
//                    {
//                        return Collections.emptySet();
//                    }
//                //查询点赞列表
//                List<LikedRecord> list = lambdaQuery()
//                        .in(LikedRecord::getBizId, bizIds)
//                        .eq(LikedRecord::getUserId, UserContext.getUser())
//                        .list();
//                return list.stream().map(LikedRecord::getBizId).collect(Collectors.toSet());
//            }
//
//        /**
//         * 点赞
//         *
//         * @param likeRecord 点赞记录
//         * @return boolean
//         */
//        private boolean liked(LikeRecordFormDTO likeRecord)
//            {
//                //查询是否已经点赞
//                Integer record = lambdaQuery()
//                        .eq(LikedRecord::getBizId, likeRecord.getBizId())
//                        .eq(LikedRecord::getBizType, likeRecord.getBizType())
//                        .eq(LikedRecord::getUserId, UserContext.getUser())
//                        .count();
//                if (record > 0)
//                    {
//                        //已经点赞
//                        return false;
//                    }
//                //未点赞
//                LikedRecord like = BeanUtils.copyBean(likeRecord, LikedRecord.class);
//                return save(like.setUserId(UserContext.getUser()));
//            }
//
//        /**
//         * 取消点赞
//         *
//         * @param likeRecord 点赞记录
//         * @return boolean
//         */
//        private boolean unlike(LikeRecordFormDTO likeRecord)
//            {
//                //查询是否已经点赞
//                Integer record = lambdaQuery()
//                        .eq(LikedRecord::getBizId, likeRecord.getBizId())
//                        .eq(LikedRecord::getBizType, likeRecord.getBizType())
//                        .eq(LikedRecord::getUserId, UserContext.getUser())
//                        .count();
//                if (record <= 0)
//                    {
//                        //未点赞
//                        return false;
//                    }
//                //已经点赞
//                return lambdaUpdate()
//                        .eq(LikedRecord::getBizId, likeRecord.getBizId())
//                        .eq(LikedRecord::getUserId, UserContext.getUser())
//                        .remove();
//            }
//    }
