package com.tianji.remark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.remark.constant.RedisConstants;
import com.tianji.remark.domain.dto.LikeRecordFormDTO;
import com.tianji.remark.domain.dto.LikedTimesDTO;
import com.tianji.remark.domain.po.LikedRecord;
import com.tianji.remark.mapper.LikedRecordMapper;
import com.tianji.remark.service.ILikedRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Encounter
 * @date 2024/11/29 17:18<br/>
 * 点赞记录表(tj_remark.liked_record)表服务实现类
 * <p>
 * 使用 Redis 实现点赞记录表服务
 */
@Service
@RequiredArgsConstructor
public class LikedRecordServiceRedisImpl extends ServiceImpl<LikedRecordMapper, LikedRecord> implements ILikedRecordService
    {
        private final RabbitMqHelper mqHelper;
        private final StringRedisTemplate redisTemplate;
        
        /**
         * 添加 Like Record
         *
         * @param likeRecordFormDTO LIKE 记录表 DTO
         */
        @Override
        public void addLikeRecord(LikeRecordFormDTO likeRecordFormDTO)
            {
                //先判断当前要执行的操作是点赞还是取消点赞
                boolean flag = likeRecordFormDTO.getLiked()
                        ? liked(likeRecordFormDTO)
                        : unlike(likeRecordFormDTO);
                
                //是否执行成功
                if (!flag)
                    {
                        log.error("点赞业务执行失败");
                        return;
                    }
                
                //执行成功，统计点赞数
                Long count = redisTemplate.opsForSet()
                        .size(RedisConstants.LIKE_BIZ_KEY_PREFIX + likeRecordFormDTO.getBizId());
                if (count == null)
                    {
                        return;
                    }
                
                //缓存总点赞数
                redisTemplate.opsForZSet()
                        .add(RedisConstants.LIKES_TIMES_KEY_PREFIX + likeRecordFormDTO.getBizType(),
                                likeRecordFormDTO.getBizId().toString(),
                                count);
            }
        
        /**
         * 批量查询点赞列表
         *
         * @param bizIds 业务 ID
         * @return {@link Set }<{@link Long }>
         */
        @Override
        public Set<Long> likedList(List<Long> bizIds)
            {
                if (bizIds.isEmpty())
                    {
                        return Collections.emptySet();
                    }
                //获取当前用户
                Long userId = UserContext.getUser();
                //查询点赞状态
                List<Object> objects = redisTemplate.executePipelined((RedisCallback<Object>) connection ->
                    {
                        StringRedisConnection src = (StringRedisConnection) connection;
                        for (Long bizId : bizIds)
                            {
                                String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + bizId;
                                src.sIsMember(key, userId.toString());
                            }
                        return null;
                    });
                
                //返回结果
                return IntStream.range(0, objects.size())
                        //过滤出点过赞的业务id
                        .filter(i -> (Boolean) objects.get(i))
                        //获取点过赞的业务id
                        .mapToObj(bizIds::get)
                        //收集结果
                        .collect(Collectors.toSet());
            }
        
        /**
         * 查询点赞数并发送消息
         *
         * @param bizType    业务类型
         * @param maxBizSize 最大业务规模
         */
        @Override
        public void readLikedTimesAndSendMessage(String bizType, int maxBizSize)
            {
                //读取并移除redis中缓存的点赞总数
                String key = RedisConstants.LIKES_TIMES_KEY_PREFIX + bizType;
                Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().popMin(key, maxBizSize);
                if (tuples == null || tuples.isEmpty())
                    {
                        return;
                    }
                
                //数据转换
                List<LikedTimesDTO> list = new ArrayList<>(tuples.size());
                for (ZSetOperations.TypedTuple<String> tuple : tuples)
                    {
                        String bizId = tuple.getValue();
                        Double likedTimes = tuple.getScore();
                        if (bizId == null || likedTimes == null)
                            {
                                //数据异常 跳过
                                continue;
                            }
                        list.add(new LikedTimesDTO(Long.parseLong(bizId), likedTimes.intValue()));
                    }
                
                //发送消息
                mqHelper.send(MqConstants.Exchange.LIKE_RECORD_EXCHANGE,
                        StringUtils.format(MqConstants.Key.LIKED_TIMES_KEY_TEMPLATE, bizType),
                        list);
            }
        
        /**
         * 点赞
         *
         * @param likeRecord 点赞记录
         * @return boolean
         */
        private boolean liked(LikeRecordFormDTO likeRecord)
            {
                //获取当前用户
                Long userId = UserContext.getUser();
                //获取key
                String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + likeRecord.getBizId();
                //执行SADD操作
                Long add = redisTemplate.opsForSet().add(key, userId.toString());
                //返回结果
                return add != null && add > 0;
            }
        
        /**
         * 取消点赞
         *
         * @param likeRecord 点赞记录
         * @return boolean
         */
        private boolean unlike(LikeRecordFormDTO likeRecord)
            {
                //获取当前用户
                Long userId = UserContext.getUser();
                //获取key
                String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + likeRecord.getBizId();
                //执行SREM操作
                Long remove = redisTemplate.opsForSet().remove(key, userId.toString());
                //返回结果
                return remove != null && remove > 0;
            }
    }
