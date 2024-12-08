package com.tianji.promotion.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tianji.promotion.enums.CouponStatus;
import com.tianji.promotion.enums.DiscountType;
import com.tianji.promotion.enums.ObtainType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 优惠券的规则信息
 */
@Schema(description = "优惠券的规则信息")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "coupon")
public class Coupon implements Serializable
    {
        private static final long serialVersionUID = 1L;
        /**
         * 优惠券id
         */
        @TableId(value = "id", type = IdType.ASSIGN_ID)
        @Schema(description = "优惠券id")
        private Long id;
        /**
         * 优惠券名称，可以和活动名称保持一致
         */
        @TableField(value = "`name`")
        @Schema(description = "优惠券名称，可以和活动名称保持一致")
        private String name;
        /**
         * 优惠券类型，1：普通券。目前就一种，保留字段
         */
        @TableField(value = "`type`")
        @Schema(description = "优惠券类型，1：普通券。目前就一种，保留字段")
        private Integer type;
        /**
         * 折扣类型，1：满减，2：每满减，3：折扣，4：无门槛
         */
        @TableField(value = "discount_type")
        @Schema(description = "折扣类型，1：满减，2：每满减，3：折扣，4：无门槛")
        private DiscountType discountType;
        /**
         * 是否限定作用范围，false：不限定，true：限定。默认false
         */
        @TableField(value = "`specific`")
        @Schema(description = "是否限定作用范围，false：不限定，true：限定。默认false")
        private Boolean specific;
        /**
         * 折扣值，如果是满减则存满减金额，如果是折扣，则存折扣率，8折就是存80
         */
        @TableField(value = "discount_value")
        @Schema(description = "折扣值，如果是满减则存满减金额，如果是折扣，则存折扣率，8折就是存80")
        private Integer discountValue;
        /**
         * 使用门槛，0：表示无门槛，其他值：最低消费金额
         */
        @TableField(value = "threshold_amount")
        @Schema(description = "使用门槛，0：表示无门槛，其他值：最低消费金额")
        private Integer thresholdAmount;
        /**
         * 最高优惠金额，满减最大，0：表示没有限制，不为0，则表示该券有金额的限制
         */
        @TableField(value = "max_discount_amount")
        @Schema(description = "最高优惠金额，满减最大，0：表示没有限制，不为0，则表示该券有金额的限制")
        private Integer maxDiscountAmount;
        /**
         * 获取方式：1：手动领取，2：兑换码
         */
        @TableField(value = "obtain_way")
        @Schema(description = "获取方式：1：手动领取，2：兑换码")
        private ObtainType obtainWay;
        /**
         * 开始发放时间
         */
        @TableField(value = "issue_begin_time")
        @Schema(description = "开始发放时间")
        private LocalDateTime issueBeginTime;
        /**
         * 结束发放时间
         */
        @TableField(value = "issue_end_time")
        @Schema(description = "结束发放时间")
        private LocalDateTime issueEndTime;
        /**
         * 优惠券有效期天数，0：表示有效期是指定有效期的
         */
        @TableField(value = "term_days")
        @Schema(description = "优惠券有效期天数，0：表示有效期是指定有效期的")
        private Integer termDays;
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
        private LocalDateTime termEndTime;
        /**
         * 优惠券配置状态，1：待发放，2：未开始   3：进行中，4：已结束，5：暂停
         */
        @TableField(value = "`status`")
        @Schema(description = "优惠券配置状态，1：待发放，2：未开始   3：进行中，4：已结束，5：暂停")
        private CouponStatus status;
        /**
         * 总数量，不超过5000
         */
        @TableField(value = "total_num")
        @Schema(description = "总数量，不超过5000")
        private Integer totalNum;
        /**
         * 已发行数量，用于判断是否超发
         */
        @TableField(value = "issue_num")
        @Schema(description = "已发行数量，用于判断是否超发")
        private Integer issueNum;
        /**
         * 已使用数量
         */
        @TableField(value = "used_num")
        @Schema(description = "已使用数量")
        private Integer usedNum;
        /**
         * 每个人限领的数量，默认1
         */
        @TableField(value = "user_limit")
        @Schema(description = "每个人限领的数量，默认1")
        private Integer userLimit;
        /**
         * 拓展参数字段，保留字段
         */
        @TableField(value = "ext_param")
        @Schema(description = "拓展参数字段，保留字段")
        private String extParam;
        /**
         * 创建时间
         */
        @TableField(value = "create_time")
        @Schema(description = "创建时间")
        private LocalDateTime createTime;
        /**
         * 更新时间
         */
        @TableField(value = "update_time")
        @Schema(description = "更新时间")
        private LocalDateTime updateTime;
        /**
         * 创建人
         */
        @TableField(value = "creater")
        @Schema(description = "创建人")
        private Long creater;
        /**
         * 更新人
         */
        @TableField(value = "updater")
        @Schema(description = "更新人")
        private Long updater;
    }