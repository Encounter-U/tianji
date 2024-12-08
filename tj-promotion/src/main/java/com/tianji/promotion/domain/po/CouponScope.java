package com.tianji.promotion.domain.po;

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
 * 优惠券作用范围信息
 */
@Schema(description = "优惠券作用范围信息")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "coupon_scope")
public class CouponScope implements Serializable
    {
        private static final long serialVersionUID = 1L;
        @TableId(value = "id", type = IdType.ASSIGN_ID)
        @Schema(description = "")
        private Long id;
        /**
         * 范围限定类型：1-分类，2-课程，等等
         */
        @TableField(value = "`type`")
        @Schema(description = "范围限定类型：1-分类，2-课程，等等")
        private Integer type;
        /**
         * 优惠券id
         */
        @TableField(value = "coupon_id")
        @Schema(description = "优惠券id")
        private Long couponId;
        /**
         * 优惠券作用范围的业务id，例如分类id、课程id
         */
        @TableField(value = "biz_id")
        @Schema(description = "优惠券作用范围的业务id，例如分类id、课程id")
        private Long bizId;
    }