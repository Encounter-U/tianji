package com.tianji.promotion.handler;

import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.promotion.constants.PromotionConstants;
import com.tianji.promotion.domain.dto.CouponIssueFormDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.enums.CouponStatus;
import com.tianji.promotion.service.ICouponService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Encounter
 * @date 2024/12/08 22:05<br/>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueHandler
    {
        private final ICouponService couponService;
        private final StringRedisTemplate redisTemplate;
        
        /**
         * 开始发行优惠券
         */
        @XxlJob("BeginIssueCoupon")
        public void beginIssueCoupon()
            {
                //更新状态为发行中
                log.info("开始定时发行优惠券");
                List<Coupon> coupons = couponService.lambdaQuery()
                        .eq(Coupon::getStatus, CouponStatus.UN_ISSUE)
                        .le(Coupon::getIssueBeginTime, LocalDateTime.now())
                        .ge(Coupon::getIssueEndTime, LocalDateTime.now())
                        .list();
                
                if (CollUtils.isEmpty(coupons))
                    {
                        log.info("没有需要发行的优惠券");
                        return;
                    }
                
                //转换为CouponFormDTO并调用方法开始发行并存入缓存
                coupons.parallelStream()
                        .map(c -> BeanUtils.copyProperties(c, CouponIssueFormDTO.class))
                        .forEach(couponService::issueCoupon);
                log.info("定时发行优惠券任务结束");
            }
        
        /**
         * 定时停止发行优惠券
         */
        @XxlJob("StopIssueCoupon")
        public void pauseIssueCoupon()
            {
                log.info("开始定时停止发行优惠券");
                //更新状态为发行结束
                couponService.lambdaUpdate()
                        .eq(Coupon::getStatus, CouponStatus.ISSUING)
                        .le(Coupon::getIssueEndTime, LocalDateTime.now())
                        .set(Coupon::getStatus, CouponStatus.FINISHED)
                        .update();
                
                //查询是否有已暂停或已结束的优惠券
                List<Coupon> coupons = couponService.lambdaQuery()
                        .in(Coupon::getStatus, CouponStatus.PAUSE, CouponStatus.FINISHED)
                        .list();
                if (CollUtils.isEmpty(coupons))
                    {
                        log.info("没有优惠券缓存需清理");
                        return;
                    }
                
                //清理缓存
                coupons.parallelStream()
                        .map(Coupon::getId)
                        .forEach(id -> redisTemplate.delete(PromotionConstants.COUPON_CACHE_KEY_PREFIX + id));
            }
    }
