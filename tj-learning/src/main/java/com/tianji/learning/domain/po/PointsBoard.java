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

/**
 * 学霸天梯榜
 */
@Schema(description = "学霸天梯榜")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "points_board")
public class PointsBoard implements Serializable
    {
        private static final long serialVersionUID = 1L;
        /**
         * 榜单id
         */
        @TableId(value = "id", type = IdType.INPUT)
        @Schema(description = "榜单id")
        private Long id;
        /**
         * 学生id
         */
        @TableField(value = "user_id")
        @Schema(description = "学生id")
        private Long userId;
        /**
         * 积分值
         */
        @TableField(value = "points")
        @Schema(description = "积分值")
        private Integer points;
        /**
         * 名次，只记录赛季前100
         */
        @TableField(value = "`rank`", exist = false)
        @Schema(description = "名次，只记录赛季前100")
        private Integer rank;
        /**
         * 赛季，例如 1,就是第一赛季，2-就是第二赛季
         */
        @TableField(value = "season", exist = false)
        @Schema(description = "赛季，例如 1,就是第一赛季，2-就是第二赛季")
        private Integer season;
    }