package com.tianji.learning.controller;

import com.tianji.learning.domain.vo.PointsStatisticsVO;
import com.tianji.learning.service.IPointsRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学习积分记录，每个月底清零(tj_learning.points_record)表控制层
 *
 * @author Encounter
 * @date 2024-12-02
 */
@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
@Tag(name = "学习积分记录", description = "学习积分记录，每个月底清零")
public class PointsRecordController
    {
        //服务对象
        private final IPointsRecordService pointsRecordService;
        
        /**
         * 查询 Today 积分
         *
         * @return {@link List }<{@link PointsStatisticsVO }>
         */
        @GetMapping("today")
        @Operation(summary = "获取今天的积分")
        public List<PointsStatisticsVO> queryTodayPoints()
            {
                return pointsRecordService.queryTodayPoints();
            }
    }
