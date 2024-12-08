package com.tianji.promotion.handler;

import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.enums.CouponStatus;
import com.tianji.promotion.service.ICouponService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author Encounter
 * @date 2024/12/08 22:05<br/>
 */
@Component
@RequiredArgsConstructor
public class CouponIssueHandler
    {
        private final ICouponService couponService;
        
        /**
         * 开始发行优惠券
         */
        @XxlJob("BeginIssueCoupon")
        public void beginIssueCoupon()
            {
                couponService.lambdaUpdate()
                        .eq(Coupon::getStatus, CouponStatus.UN_ISSUE)
                        .le(Coupon::getIssueBeginTime, LocalDateTime.now())
                        .ge(Coupon::getIssueEndTime, LocalDateTime.now())
                        .set(Coupon::getStatus, CouponStatus.ISSUING)
                        .update();
            }
        
        /**
         * 暂停发行优惠券
         */
        @XxlJob("PauseIssueCoupon")
        public void pauseIssueCoupon()
            {
                couponService.lambdaUpdate()
                        .eq(Coupon::getStatus, CouponStatus.ISSUING)
                        .lt(Coupon::getIssueBeginTime, LocalDateTime.now())
                        .set(Coupon::getStatus, CouponStatus.UN_ISSUE)
                        .update();
            }
    }
