package com.tianji.learning.controller;

import com.tianji.learning.service.IPointsBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学霸天梯榜(tj_learning.points_board)表控制层
 *
 * @author Encounter
 * @date 2024-12-02
 */
@RestController
@RequestMapping("/points_boards")
@RequiredArgsConstructor
public class PointsBoardController
    {
        //服务对象
        private final IPointsBoardService pointsBoardService;
        
    }
