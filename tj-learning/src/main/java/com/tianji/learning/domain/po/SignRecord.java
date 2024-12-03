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

/**
 * 签到记录表
 */
@Schema(description = "签到记录表")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sign_record")
public class SignRecord implements Serializable
    {
        private static final long serialVersionUID = 1L;
        /**
         * 主键
         */
        @TableId(value = "id", type = IdType.AUTO)
        @Schema(description = "主键")
        private Long id;
        /**
         * 用户id
         */
        @TableField(value = "user_id")
        @Schema(description = "用户id")
        private Long userId;
        /**
         * 签到年份
         */
        @TableField(value = "`year`")
        @Schema(description = "签到年份")
        private Object year;
        /**
         * 签到月份
         */
        @TableField(value = "`month`")
        @Schema(description = "签到月份")
        private Integer month;
        /**
         * 签到日期
         */
        @TableField(value = "`date`")
        @Schema(description = "签到日期")
        private LocalDate date;
        /**
         * 是否补签
         */
        @TableField(value = "is_backup")
        @Schema(description = "是否补签")
        private Boolean isBackup;
    }