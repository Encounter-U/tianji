package com.tianji.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.learning.domain.po.PointsBoard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Encounter
 * @date 2024/12/02 15:30 <br>
 */
@Mapper
public interface PointsBoardMapper extends BaseMapper<PointsBoard>
    {
        /**
         * 创建积分板表
         *
         * @param tableName 表名称
         */
        void createPointsBoardTable(@Param("tableName") String tableName);
        
    }