package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.learning.domain.po.PointsBoardSeason;
import com.tianji.learning.domain.vo.PointsBoardSeasonVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Encounter
 * @date 2024/12/02 15:29 <br>
 */
public interface IPointsBoardSeasonService extends IService<PointsBoardSeason>
    {
        /**
         * 查询开始时间在当前时间之前的赛季
         *
         * @return {@link List }<{@link PointsBoardSeasonVO }>
         */
        List<PointsBoardSeasonVO> listBeforeNow();
        
        /**
         * 查询赛季id由时间
         *
         * @param time 时间
         * @return {@link Integer }
         */
        Integer querySeasonByTime(LocalDateTime time);
    }
