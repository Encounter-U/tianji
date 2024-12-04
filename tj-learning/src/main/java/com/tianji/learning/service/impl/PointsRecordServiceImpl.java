package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.DateUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.domain.po.PointsRecord;
import com.tianji.learning.domain.vo.PointsStatisticsVO;
import com.tianji.learning.enums.PointsRecordType;
import com.tianji.learning.mapper.PointsRecordMapper;
import com.tianji.learning.service.IPointsRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.tianji.learning.constants.LearningConstants.POINTS_RECORD_TABLE_PREFIX;

/**
 * @author Encounter
 * @date 2024/12/02 15:29 <br>
 */
@Service
@RequiredArgsConstructor
public class PointsRecordServiceImpl extends ServiceImpl<PointsRecordMapper, PointsRecord> implements IPointsRecordService
    {
        private final PointsRecordMapper recordMapper;
        private final StringRedisTemplate redisTemplate;
        
        /**
         * 添加积分记录
         *
         * @param userId 用户id
         * @param points 积分
         * @param type   积分记录种类
         */
        @Override
        public void addPointsRecord(Long userId, int points, PointsRecordType type)
            {
                LocalDateTime now = LocalDateTime.now();
                //最大积分
                int maxPoints = type.getMaxPoints();
                //实际积分
                int realPoints = points;
                //判断当前方式是否有上限
                if (maxPoints > 0)
                    {
                        //有，判断是否超过上限
                        LocalDateTime begin = DateUtils.getDayStartTime(now);
                        LocalDateTime end = DateUtils.getDayEndTime(now);
                        //查询今天积分是否超过上限
                        int currentPoints = queryUserPointsByTypeAndDate(userId, type, begin, end);
                        //判断是否超过上限
                        if (currentPoints > maxPoints)
                            {
                                //超过上限，直接结束
                                return;
                            }
                        
                        //没有超过上限，添加积分记录
                        if (currentPoints + points > maxPoints)
                            {
                                //超过上限，实际积分为上限减去当前积分
                                realPoints = maxPoints - currentPoints;
                            }
                    }
                //添加积分记录
                PointsRecord record = new PointsRecord();
                record.setUserId(userId);
                record.setPoints(realPoints);
                record.setType(type);
                save(record);
                
                //更新积分到redis
                String key = RedisConstants.POINTS_RECORD_KEY_PREFIX + now.format(DateUtils.SIGN_DATE_SUFFIX_FORMATTER);
                redisTemplate.opsForZSet().incrementScore(key, String.valueOf(userId), realPoints);
            }
        
        /**
         * 查询 Today 积分
         *
         * @return {@link List }<{@link PointsStatisticsVO }>
         */
        @Override
        public List<PointsStatisticsVO> queryTodayPoints()
            {
                //获取今天日期
                LocalDateTime today = LocalDateTime.now();
                LocalDateTime begin = DateUtils.getDayStartTime(today);
                LocalDateTime end = DateUtils.getDayEndTime(today);
                
                //从记录表中查询所有有关信息，构建查询条件
                QueryWrapper<PointsRecord> wrapper = new QueryWrapper<>();
                wrapper.lambda()
                        .between(PointsRecord::getCreateTime, begin, end)
                        .eq(PointsRecord::getUserId, UserContext.getUser());
                //调用mapper
                List<PointsRecord> pointsRecords = recordMapper.queryByUserAndDate(wrapper);
                
                if (CollUtils.isEmpty(pointsRecords))
                    {
                        //没有记录，直接返回
                        return CollUtils.emptyList();
                    }
                
                //构建返回结果
                List<PointsStatisticsVO> result = new ArrayList<>();
                
                //遍历所有记录
                for (PointsRecord record : pointsRecords)
                    {
                        //封装返回结果
                        PointsStatisticsVO vo = new PointsStatisticsVO();
                        vo.setType(record.getType().getDesc());
                        vo.setPoints(record.getPoints());
                        vo.setMaxPoints(record.getType().getMaxPoints());
                        //添加到结果集
                        result.add(vo);
                    }
                return result;
            }
        
        /**
         * 按赛季创建积分记录表
         *
         * @param season 赛季
         */
        @Override
        public void createPointsRecordTableBySeason(Integer season)
            {
                recordMapper.createPointsRecordTableBySeason(POINTS_RECORD_TABLE_PREFIX + season);
            }
        
        /**
         * 查询上个月积分记录
         *
         * @param pageNo   页码
         * @param pageSize 页面大小
         * @param begin    开始
         * @param end      结束
         * @return {@link List }<{@link PointsRecord }>
         */
        @Override
        public List<PointsRecord> queryLastMonthPointsRecord(int pageNo, int pageSize, LocalDateTime begin, LocalDateTime end)
            {
                Page<PointsRecord> page = lambdaQuery()
                        .between(begin != null && end != null, PointsRecord::getCreateTime, begin, end)
                        .page(Page.of(pageNo, pageSize));
                return page.getRecords();
            }
        
        /**
         * 删除原表中上个赛季的积分明细
         *
         * @param begin 开始
         * @param end   结束
         */
        @Override
        public void removeByTime(LocalDateTime begin, LocalDateTime end)
            {
                lambdaUpdate()
                        .between(PointsRecord::getCreateTime, begin, end)
                        .remove();
            }
        
        /**
         * 按类型和日期查询用户积分
         *
         * @param userId 用户id
         * @param type   积分记录种类
         * @param begin  开始
         * @param end    结束
         * @return int
         */
        private int queryUserPointsByTypeAndDate(Long userId, PointsRecordType type, LocalDateTime begin, LocalDateTime end)
            {
                //构建查询条件
                QueryWrapper<PointsRecord> wrapper = new QueryWrapper<>();
                wrapper.lambda()
                        .eq(PointsRecord::getUserId, userId)
                        .eq(type != null, PointsRecord::getType, type)
                        .between(begin != null && end != null, PointsRecord::getCreateTime, begin, end);
                //调用mapper
                Integer points = baseMapper.queryUserPointsByTypeAndDate(wrapper);
                
                //判断并返回
                return points == null ? 0 : points;
            }
    }
