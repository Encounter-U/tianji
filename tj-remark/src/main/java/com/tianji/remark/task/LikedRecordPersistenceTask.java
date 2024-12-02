package com.tianji.remark.task;

import com.tianji.common.utils.BizContext;
import com.tianji.remark.constant.RedisConstants;
import com.tianji.remark.domain.po.LikedRecord;
import com.tianji.remark.mapper.LikedRecordMapper;
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
        //private final ThreadLocal<Long> bizCount = new ThreadLocal<>();
        private final StringRedisTemplate redisTemplate;
        private final LikedRecordMapper recordMapper;
        
        /**
         * 持久化点赞记录
         */
        @Scheduled(cron = "0 */5 * * * ?")
        public void persistenceLikedRecord()
            {
                //log.debug("开始持久化点赞记录");
                
                //获取redis里的点赞记录
                //获取key
                String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + "*";
                //获取所有的key
                Set<byte[]> keys = redisTemplate.execute((RedisCallback<Set<byte[]>>) connection -> connection.keys(key.getBytes()));
                if (keys == null || keys.isEmpty())
                    {
                        return;
                    }
                
                List<LikedRecord> likedRecords = new ArrayList<>();
                
                /*if (bizCount.get() == null)
                    {
                        bizCount.set(0L);
                    }
                //获取已经存在的点赞记录
                Long lastCount = bizCount.get();*/
                
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
                                LikedRecord likedRecord = new LikedRecord();
                                likedRecord.setBizId(Long.parseLong(bizId));
                                likedRecord.setUserId(Long.parseLong(userId));
                                likedRecord.setBizType(bizType);
                                //log.debug("持久化点赞类型：{}", bizType);
                                likedRecords.add(likedRecord);
                            }
                    }
                //if (likedRecords != null && likedRecords.size() != lastCount)
                
                //bizCount.set((long) likedRecords.size());
                
                log.debug("持久化点赞记录：{}", likedRecords);
                //批量插入 不存在则插入 存在则忽略
                recordMapper.insertBatchIfNotExist(likedRecords);
                
                //log.debug("点赞记录持久化完成");
            }
    }
