package com.tianji.learning.handle;

import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.DateUtils;
import com.tianji.learning.constants.LearningConstants;
import com.tianji.learning.domain.po.PointsRecord;
import com.tianji.learning.service.IPointsBoardSeasonService;
import com.tianji.learning.service.IPointsRecordService;
import com.tianji.learning.utils.TableInfoContext;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Encounter
 * @date 2024/12/04 16:20<br/>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PointsRecordPersistentHandler
    {
        private final StringRedisTemplate redisTemplate;
        private final IPointsRecordService recordService;
        private final IPointsBoardSeasonService seasonService;
        
        /**
         * 创建历史赛季积分明细表
         */
        @XxlJob("createPointsRecordTableJob")
        public void createTableJob()
            {
                //获取上个月时间
                LocalDateTime time = LocalDateTime.now().minusMonths(1);
                //查询赛季id
                Integer season = seasonService.querySeasonByTime(time);
                if (season == null)
                    {
                        //赛季不存在
                        log.debug("赛季不存在，无法创建历史赛季积分明细表");
                        return;
                    }
                //创建上个月积分明细表 points_record_11
                recordService.createPointsRecordTableBySeason(season);
            }
        
        /**
         * 保存积分明细记录2 dB
         */
        @XxlJob("savePointsRecord2DB")
        public void savePointsRecord2DB()
            {
                //获取上个月起始日期
                LocalDate time = LocalDate.now().minusMonths(1);
                LocalDateTime begin = DateUtils.getMonthBeginTime(time);
                LocalDateTime end = DateUtils.getMonthEndTime(time);
                //查询上个月赛季信息
                Integer season = seasonService.querySeasonByTime(begin);
                
                //保存到数据库
                int index = XxlJobHelper.getShardIndex();
                int total = XxlJobHelper.getShardTotal();
                int pageNo = index + 1;
                int pageSize = 100;
                while (true)
                    {
                        List<PointsRecord> pointsRecordList = recordService.queryLastMonthPointsRecord(pageNo, pageSize, begin, end);
                        //判断是否有数据
                        if (CollUtils.isEmpty(pointsRecordList))
                            {
                                log.debug("上个月无积分明细数据");
                                break;
                            }
                        
                        //将表名存入ThreadLocal
                        TableInfoContext.setInfo(LearningConstants.POINTS_RECORD_TABLE_PREFIX + season);
                        //保存数据
                        recordService.saveBatch(pointsRecordList);
                        
                        //翻页
                        pageNo += total;
                    }
                //移除ThreadLocal
                TableInfoContext.remove();
            }
        
        /**
         * 从数据库清理积分明细记录
         */
        @XxlJob("clearPointsRecordFromDB")
        public void cleanPointsRecordFromDB()
            {
                //获取上个月起始日期
                LocalDate time = LocalDate.now().minusMonths(1);
                LocalDateTime begin = DateUtils.getMonthBeginTime(time);
                LocalDateTime end = DateUtils.getMonthEndTime(time);
                
                //删除原表中历史赛季积分明细
                recordService.removeByTime(begin, end);
            }
    }
