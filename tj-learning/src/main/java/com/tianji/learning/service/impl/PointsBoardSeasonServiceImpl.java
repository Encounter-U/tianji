package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.learning.domain.po.PointsBoardSeason;
import com.tianji.learning.domain.vo.PointsBoardSeasonVO;
import com.tianji.learning.mapper.PointsBoardSeasonMapper;
import com.tianji.learning.service.IPointsBoardSeasonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Encounter
 * @date 2024/12/02 15:29 <br>
 */
@Slf4j
@Service
public class PointsBoardSeasonServiceImpl extends ServiceImpl<PointsBoardSeasonMapper, PointsBoardSeason> implements IPointsBoardSeasonService
    {
        /**
         * 查询开始时间在当前时间之前的赛季列表
         *
         * @return {@link List }<{@link PointsBoardSeasonVO }>
         */
        @Override
        public List<PointsBoardSeasonVO> listBeforeNow()
            {
                //获取当前时间
                LocalDate now = LocalDate.now();
                //查询开始时间在当前时间之前的赛季列表
                List<PointsBoardSeason> list = lambdaQuery()
                        .le(PointsBoardSeason::getBeginTime, now)
                        .list();
                if (list == null)
                    {
                        return CollUtils.emptyList();
                    }
                
                List<PointsBoardSeasonVO> voList = new ArrayList<>();
                //转换为VO对象
                for (PointsBoardSeason pointsBoardSeason : list)
                    {
                        PointsBoardSeasonVO vo = BeanUtils.copyBean(pointsBoardSeason, PointsBoardSeasonVO.class);
                        voList.add(vo);
                    }
                return voList;
            }
        
        /**
         * 查询赛季id由时间
         *
         * @param time 时间
         * @return {@link Integer }
         */
        @Override
        public Integer querySeasonByTime(LocalDateTime time)
            {
                //查询赛季id
                Optional<PointsBoardSeason> pointsBoardSeason = lambdaQuery()
                        .le(PointsBoardSeason::getBeginTime, time)
                        .ge(PointsBoardSeason::getEndTime, time)
                        .oneOpt();
                //返回赛季id
                return pointsBoardSeason.map(PointsBoardSeason::getId).orElse(null);
            }
    }
