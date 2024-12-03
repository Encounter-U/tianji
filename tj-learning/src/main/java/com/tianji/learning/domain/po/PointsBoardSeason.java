package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

@Schema
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "points_board_season")
public class PointsBoardSeason implements Serializable
    {
        private static final long serialVersionUID = 1L;
        /**
         * 自增长id，season标示
         */
        @TableId(value = "id", type = IdType.ASSIGN_ID)
        @Schema(description = "自增长id，season标示")
        private Integer id;
        /**
         * 赛季名称，例如：第1赛季
         */
        @TableField(value = "`name`")
        @Schema(description = "赛季名称，例如：第1赛季")
        private String name;
        /**
         * 赛季开始时间
         */
        @TableField(value = "begin_time")
        @Schema(description = "赛季开始时间")
        private LocalDate beginTime;
        /**
         * 赛季结束时间
         */
        @TableField(value = "end_time")
        @Schema(description = "赛季结束时间")
        private LocalDate endTime;
    }