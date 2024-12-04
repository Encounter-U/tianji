package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.learning.domain.po.PointsBoard;
import com.tianji.learning.domain.query.PointsBoardQuery;
import com.tianji.learning.domain.vo.PointsBoardVO;

import java.util.List;

/**
 * @author Encounter
 * @date 2024/12/02 15:29 <br>
 */
public interface IPointsBoardService extends IService<PointsBoard>
    {
        
        
        /**
         * 查询排行榜
         *
         * @param query 查询
         * @return {@link PointsBoardVO }
         */
        PointsBoardVO queryPointsBoard(PointsBoardQuery query);
        
        /**
         * 按赛季创建积分板表
         *
         * @param seasonId 赛季id
         */
        void createPointsBoardTableBySeason(Integer seasonId);
        
        /**
         * 查询当前赛季积分板
         *
         * @param key      键
         * @param pageNo   页码
         * @param pageSize 页面大小
         * @return {@link List }<{@link PointsBoard }>
         */
        List<PointsBoard> queryCurrentSeasonPointsBoard(String key, Integer pageNo, Integer pageSize);
    }
