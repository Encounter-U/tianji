package com.tianji.promotion.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.promotion.domain.query.UserCouponQuery;
import com.tianji.promotion.domain.vo.CouponVO;
import com.tianji.promotion.service.IUserCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户领取优惠券的记录，是真正使用的优惠券信息(tj_promotion.user_coupon)表控制层
 *
 * @author Encounter
 * @date 2024-12-09
 */
@RestController
@RequestMapping("/user-coupons")
@RequiredArgsConstructor
@Tag(name = "用户端优惠券管理")
public class UserCouponController
    {
        //服务对象
        private final IUserCouponService userCouponService;
        
        /**
         * 领取发放中的优惠券
         *
         * @param id id
         */
        @PostMapping("/{id}/receive")
        @Operation(summary = "领取发放中的优惠券")
        public void receiveCoupon(@PathVariable Long id)
            {
                userCouponService.receiveCoupon(id);
            }
        
        /**
         * 兑换码兑换优惠券
         *
         * @param code 兑换码
         */
        @PostMapping("/{code}/exchange")
        @Operation(summary = "兑换码兑换优惠券")
        public void exchangeCoupon(@PathVariable String code)
            {
                userCouponService.exchangeCoupon(code);
            }
        
        /**
         * 分页查询我的优惠券
         *
         * @param query 查询条件
         * @return {@link PageDTO }<{@link CouponVO }>
         */
        @GetMapping("/page")
        @Operation(summary = "分页查询用户优惠券")
        public PageDTO<CouponVO> queryMyCoupon(UserCouponQuery query)
            {
                return userCouponService.queryMyCoupon(query);
            }
    }
