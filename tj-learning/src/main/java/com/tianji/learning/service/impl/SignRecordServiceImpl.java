package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BooleanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.DateUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.domain.po.SignRecord;
import com.tianji.learning.domain.vo.SignResultVO;
import com.tianji.learning.mapper.SignRecordMapper;
import com.tianji.learning.mq.message.SignInMessage;
import com.tianji.learning.service.ISignRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Encounter
 * @date 2024/12/02 16:03 <br>
 */
@Service
@RequiredArgsConstructor
public class SignRecordServiceImpl extends ServiceImpl<SignRecordMapper, SignRecord> implements ISignRecordService
    {
        private final StringRedisTemplate redisTemplate;
        private final RabbitMqHelper mqHelper;
        
        /**
         * 添加签到记录
         *
         * @return {@link SignResultVO }
         */
        @Override
        public SignResultVO addSignRecord()
            {
                //获取当前登录用户
                Long userId = UserContext.getUser();
                //获取当前日期
                LocalDate now = LocalDate.now();
                //拼接key
                String key = RedisConstants.SIGN_RECORD_KEY_PREFIX +
                        userId +
                        now.format(DateUtils.SIGN_DATE_SUFFIX_FORMATTER);
                //计算offset 从0开始
                int offset = now.getDayOfMonth() - 1;
                //保存签到信息
                Boolean exist = redisTemplate.opsForValue().setBit(key, offset, true);
                if (BooleanUtils.isTrue(exist))
                    {
                        //已经签到
                        throw new BizIllegalException("不允许重复签到");
                    }
                
                //计算连签天数
                int signDays = countSignDays(key, now.getDayOfMonth());
                
                //计算签到得分
                int rewardPoints = 0;
                switch (signDays)
                    {
                        case 7:
                            rewardPoints = 10;
                            break;
                        case 14:
                            rewardPoints = 20;
                            break;
                        case 28:
                            rewardPoints = 40;
                            break;
                    }
                
                //保存积分明细
                mqHelper.send(MqConstants.Exchange.LEARNING_EXCHANGE,
                        MqConstants.Key.SIGN_IN,
                        //签到积分为奖励积分（rewardPoints）+基本积分（1）
                        SignInMessage.of(userId, rewardPoints + 1));
                
                //封装返回
                SignResultVO signResultVO = new SignResultVO();
                signResultVO.setSignDays(signDays);
                signResultVO.setRewardPoints(rewardPoints);
                return signResultVO;
            }
        
        /**
         * 查询签到记录
         *
         * @return {@link Byte[] }
         */
        @Override
        public Byte[] querySignRecord()
            {
                //获取当前登录用户
                Long userId = UserContext.getUser();
                //获取当前日期
                LocalDate now = LocalDate.now();
                //拼接key
                String key = RedisConstants.SIGN_RECORD_KEY_PREFIX +
                        userId +
                        now.format(DateUtils.SIGN_DATE_SUFFIX_FORMATTER);
                //获取签到记录 从本月第一天到今天的签到情况
                //计算本月到现在是第几天
                int dayOfMonth = now.getDayOfMonth();
                List<Long> result = redisTemplate.opsForValue()
                        .bitField(key, BitFieldSubCommands.create()
                                .get(BitFieldSubCommands.BitFieldType.signed(dayOfMonth)).valueAt(0));
                
                if (CollUtils.isEmpty(result))
                    {
                        return new Byte[0];
                    }
                //遍历解析结果
                Byte[] signRecord = new Byte[dayOfMonth];
                int num = result.get(0).intValue();
                int index = dayOfMonth - 1;
                while (index >= 0)
                    {
                        signRecord[index--] = (byte) (num & 1);
                        //右移一位
                        num >>>= 1;
                    }
                return signRecord;
            }
        
        /**
         * 计算签到天数
         *
         * @param key        redis key
         * @param dayOfMonth 每月日期
         * @return int
         */
        private int countSignDays(String key, int dayOfMonth)
            {
                //获取从本月第一天开始到今天的签到情况
                List<Long> result = redisTemplate.opsForValue()
                        .bitField(key, BitFieldSubCommands.create()
                                .get(BitFieldSubCommands.BitFieldType.signed(dayOfMonth)).valueAt(0));
                
                if (CollUtils.isEmpty(result))
                    {
                        return 0;
                    }
                
                //num为签到天数
                int num = result.get(0).intValue();
                
                //定义一个变量记录连续签到天数
                int count = 0;
                //num与1做与运算，如果结果为1，说明该天签到
                while ((num & 1) == 1)
                    {
                        //连续签到天数加1
                        count++;
                        //右移一位
                        num >>>= 1;
                    }
                return count;
            }
    }
