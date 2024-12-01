package com.tianji.remark.task;

import com.tianji.common.utils.BizContext;
import com.tianji.remark.constant.RedisConstants;
import com.tianji.remark.domain.po.LikedRecord;
import com.tianji.remark.mapper.LikedRecordMapper;
import com.tianji.remark.service.ILikedRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 点赞记录持久性任务
 *
 * @author Encounter
 * @date 2024/11/30 16:18<br/>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikedRecordPersistenceTask
    {
        private final ILikedRecordService likedRecordService;
        private final StringRedisTemplate redisTemplate;
        private final LikedRecordMapper recordMapper;
        
        /**
         * 持久化点赞记录
         */
        @Scheduled(cron = "0 * * * * ?")
        public void persistenceLikedRecord()
            {
                //log.debug("开始持久化点赞记录");
                
                //获取redis里的点赞记录
                //获取key
                String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + "*";
                //获取所有的key
                redisTemplate.execute((RedisCallback<Void>) connection ->
                    {
                        Set<byte[]> keys = connection.keys(key.getBytes());
                        if (keys == null || keys.isEmpty())
                            {
                                return null;
                            }
                        
                        //先获取数据库中所有的点赞记录的主键
                        /*List<LikedRecord> records = likedRecordService.lambdaQuery()
                                .select(LikedRecord::getBizId)
                                .list();
                        Set<Long> exist = records.stream()
                                .map(LikedRecord::getBizId)
                                .collect(Collectors.toSet());*/
                        
                        List<LikedRecord> likedRecords = null;
                        List<String> keyStrs = new ArrayList<>();
                        
                        //遍历key
                        for (byte[] k : keys)
                            {
                                //获取key
                                String keyStr = new String(k);
                                //获取bizId
                                String bizId = keyStr.substring(RedisConstants.LIKE_BIZ_KEY_PREFIX.length());
                                //获取用户id
                                Set<String> userIds = redisTemplate.opsForSet().members(keyStr);
                                
                                //持久化
                                likedRecords = new ArrayList<>(bizId.length());
                                if (userIds == null)
                                    {
                                        //没有点赞记录
                                        log.error("bizId:{}没有点赞记录", bizId);
                                        continue;
                                    }
                                //获取业务类型
                                String bizType = BizContext.getBiz().get(Long.parseLong(bizId));
                                for (String userId : userIds)
                                    {
                                        //判断是否已经存在
                                        //if (!exist.contains(Long.parseLong(bizId)))
                                        LikedRecord likedRecord = new LikedRecord();
                                        likedRecord.setBizId(Long.parseLong(bizId));
                                        likedRecord.setUserId(Long.parseLong(userId));
                                        likedRecord.setBizType(bizType);
                                        //log.debug("持久化点赞类型：{}", bizType);
                                        likedRecords.add(likedRecord);
                                    }
                                keyStrs.add(keyStr);
                            }
                        if (!likedRecords.isEmpty())
                            {
                                log.debug("持久化点赞记录：{}", likedRecords);
                                //批量插入 不存在则插入 存在则忽略
                                recordMapper.insertBatchIfNotExist(likedRecords);
                                //删除redis中的key
                                redisTemplate.delete(keyStrs);
                            }
                        return null;
                    });
                
                //log.debug("点赞记录持久化完成");
            }
    }
