package com.tianji.promotion.controller;

import com.tianji.promotion.service.ICouponScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 优惠券作用范围信息(tj_promotion.coupon_scope)表控制层
 *
 * @author Encounter
 * @date 2024-12-04
 */
@RestController
@RequestMapping("/scopes")
@RequiredArgsConstructor
public class CouponScopeController
    {
        //服务对象
        private final ICouponScopeService couponScopeService;
        
    }
