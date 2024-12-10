package com.tianji.promotion.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tianji.promotion.enums.UserCouponStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户领取优惠券的记录，是真正使用的优惠券信息
 */
@Schema(description = "用户领取优惠券的记录，是真正使用的优惠券信息")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "tj_promotion.user_coupon")
public class UserCoupon implements Serializable
    {
        private static final long serialVersionUID = 1L;
        /**
         * 用户券id
         */
        @TableId(value = "id", type = IdType.ASSIGN_ID)
        @Schema(description = "用户券id")
        @NotNull(message = "用户券id不能为null")
        private Long id;
        /**
         * 优惠券的拥有者
         */
        @TableField(value = "user_id")
        @Schema(description = "优惠券的拥有者")
        @NotNull(message = "优惠券的拥有者不能为null")
        private Long userId;
        /**
         * 优惠券模板id
         */
        @TableField(value = "coupon_id")
        @Schema(description = "优惠券模板id")
        @NotNull(message = "优惠券模板id不能为null")
        private Long couponId;
        /**
         * 优惠券有效期开始时间
         */
        @TableField(value = "term_begin_time")
        @Schema(description = "优惠券有效期开始时间")
        private LocalDateTime termBeginTime;
        /**
         * 优惠券有效期结束时间
         */
        @TableField(value = "term_end_time")
        @Schema(description = "优惠券有效期结束时间")
        @NotNull(message = "优惠券有效期结束时间不能为null")
        private LocalDateTime termEndTime;
        /**
         * 优惠券使用时间（核销时间）
         */
        @TableField(value = "used_time")
        @Schema(description = "优惠券使用时间（核销时间）")
        private LocalDateTime usedTime;
        /**
         * 优惠券状态，1：未使用，2：已使用，3：已失效
         */
        @TableField(value = "`status`")
        @Schema(description = "优惠券状态，1：未使用，2：已使用，3：已失效")
        @NotNull(message = "优惠券状态，1：未使用，2：已使用，3：已失效不能为null")
        private UserCouponStatus status;
        /**
         * 创建时间
         */
        @TableField(value = "create_time")
        @Schema(description = "创建时间")
        @NotNull(message = "创建时间不能为null")
        private LocalDateTime createTime;
        /**
         * 更新时间
         */
        @TableField(value = "update_time")
        @Schema(description = "更新时间")
        @NotNull(message = "更新时间不能为null")
        private LocalDateTime updateTime;
    }