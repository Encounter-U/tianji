package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.learning.domain.po.PointsRecord;
import com.tianji.learning.domain.vo.PointsStatisticsVO;
import com.tianji.learning.enums.PointsRecordType;

import java.util.List;

/**
 * @author Encounter
 * @date 2024/12/02 15:29 <br>
 */
public interface IPointsRecordService extends IService<PointsRecord>
    {
        
        
        /**
         * 添加积分记录
         *
         * @param userId 用户id
         * @param points 积分
         * @param type   积分记录种类
         */
        void addPointsRecord(Long userId, int points, PointsRecordType type);
        
        /**
         * 查询 Today 积分
         *
         * @return {@link List }<{@link PointsStatisticsVO }>
         */
        List<PointsStatisticsVO> queryTodayPoints();
    }
