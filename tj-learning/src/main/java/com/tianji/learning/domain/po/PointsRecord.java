package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tianji.learning.enums.PointsRecordType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学习积分记录，每个月底清零
 */
@Schema(description = "学习积分记录，每个月底清零")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "points_record")
public class PointsRecord implements Serializable
    {
        private static final long serialVersionUID = 1L;
        /**
         * 积分记录表id
         */
        @TableId(value = "id", type = IdType.ASSIGN_ID)
        @Schema(description = "积分记录表id")
        private Long id;
        /**
         * 用户id
         */
        @TableField(value = "user_id")
        @Schema(description = "用户id")
        private Long userId;
        /**
         * 积分方式：1-课程学习，2-每日签到，3-课程问答， 4-课程笔记，5-课程评价
         */
        @TableField(value = "`type`")
        @Schema(description = "积分方式：1-课程学习，2-每日签到，3-课程问答， 4-课程笔记，5-课程评价")
        private PointsRecordType type;
        /**
         * 积分值
         */
        @TableField(value = "points")
        @Schema(description = "积分值")
        private Integer points;
        /**
         * 创建时间
         */
        @TableField(value = "create_time")
        @Schema(description = "创建时间")
        private LocalDateTime createTime;
    }