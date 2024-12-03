package com.tianji.learning.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.tianji.learning.domain.po.PointsRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Encounter
 * @date 2024/12/02 15:30 <br>
 */
@Mapper
public interface PointsRecordMapper extends BaseMapper<PointsRecord>
    {
        /**
         * 按类型和日期查询用户点数
         *
         * @param wrapper 查询条件
         * @return {@link Integer }
         */
        @Select("select sum(points) from points_record ${ew.customSqlSegment}")
        Integer queryUserPointsByTypeAndDate(@Param(Constants.WRAPPER) QueryWrapper<PointsRecord> wrapper);
        
        /**
         * 按用户及日期查询积分记录
         *
         * @param wrapper 查询条件
         * @return {@link List }<{@link PointsRecord }>
         */
        @Select("select type,sum(points) as points from points_record ${ew.customSqlSegment} group by type")
        List<PointsRecord> queryByUserAndDate(@Param(Constants.WRAPPER) QueryWrapper<PointsRecord> wrapper);
        
    }