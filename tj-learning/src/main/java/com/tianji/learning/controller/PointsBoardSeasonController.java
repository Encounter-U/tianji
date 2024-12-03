package com.tianji.learning.controller;

import com.tianji.learning.domain.po.PointsBoardSeason;
import com.tianji.learning.domain.vo.PointsBoardSeasonVO;
import com.tianji.learning.service.IPointsBoardSeasonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * (tj_learning.points_board_season)表控制层
 *
 * @author Encounter
 * @date 2024-12-02
 */
@RestController
@RequestMapping("/boards/seasons")
@RequiredArgsConstructor
@Tag(name = "学霸天梯榜赛季接口")
public class PointsBoardSeasonController
    {
        //服务对象
        private final IPointsBoardSeasonService pointsBoardSeasonService;
        
        /**
         * 查询赛季列表
         *
         * @return {@link List }<{@link PointsBoardSeason }>
         */
        @GetMapping("/list")
        @Operation(summary = "查询赛季列表", description = "查询学霸天梯榜赛季列表")
        public List<PointsBoardSeasonVO> list()
            {
                return pointsBoardSeasonService.listBeforeNow();
            }
    }
