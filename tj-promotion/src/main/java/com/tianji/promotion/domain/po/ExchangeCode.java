package com.tianji.promotion.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tianji.promotion.enums.ExchangeCodeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 兑换码
 */
@Schema(description = "兑换码")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "exchange_code")
public class ExchangeCode implements Serializable
    {
        private static final long serialVersionUID = 1L;
        /**
         * 兑换码id
         */
        @TableId(value = "id", type = IdType.ASSIGN_ID)
        @Schema(description = "兑换码id")
        private Integer id;
        /**
         * 兑换码
         */
        @TableField(value = "code")
        @Schema(description = "兑换码")
        private String code;
        /**
         * 兑换码状态， 1：待兑换，2：已兑换，3：兑换活动已结束
         */
        @TableField(value = "`status`")
        @Schema(description = "兑换码状态， 1：待兑换，2：已兑换，3：兑换活动已结束")
        private ExchangeCodeStatus status;
        /**
         * 兑换人
         */
        @TableField(value = "user_id")
        @Schema(description = "兑换人")
        private Long userId;
        /**
         * 兑换类型，1：优惠券，以后再添加其它类型
         */
        @TableField(value = "`type`")
        @Schema(description = "兑换类型，1：优惠券，以后再添加其它类型")
        private Integer type;
        /**
         * 兑换码目标id，例如兑换优惠券，该id则是优惠券的配置id
         */
        @TableField(value = "exchange_target_id")
        @Schema(description = "兑换码目标id，例如兑换优惠券，该id则是优惠券的配置id")
        private Long exchangeTargetId;
        /**
         * 创建时间
         */
        @TableField(value = "create_time")
        @Schema(description = "创建时间")
        private LocalDateTime createTime;
        /**
         * 兑换码过期时间
         */
        @TableField(value = "expired_time")
        @Schema(description = "兑换码过期时间")
        private LocalDateTime expiredTime;
        /**
         * 更新时间
         */
        @TableField(value = "update_time")
        @Schema(description = "更新时间")
        private LocalDateTime updateTime;
    }