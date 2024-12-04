package com.tianji.learning.controller;

import com.tianji.learning.domain.query.PointsBoardQuery;
import com.tianji.learning.domain.vo.PointsBoardVO;
import com.tianji.learning.service.IPointsBoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学霸天梯榜(tj_learning.points_board)表控制层
 *
 * @author Encounter
 * @date 2024-12-02
 */
@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
@Tag(name = "排行榜功能")
public class PointsBoardController
    {
        //服务对象
        private final IPointsBoardService pointsBoardService;
        
        /**
         * 分页查询指定赛季排行榜
         *
         * @param query 查询条件
         * @return {@link PointsBoardVO }
         */
        @GetMapping
        @Operation(summary = "排行榜查询", description = "分页查询指定赛季排行榜")
        public PointsBoardVO queryPointsBoard(PointsBoardQuery query)
            {
                return pointsBoardService.queryPointsBoard(query);
            }
    }
