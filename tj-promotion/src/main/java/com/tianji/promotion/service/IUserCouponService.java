package com.tianji.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.UserCoupon;
import com.tianji.promotion.domain.query.UserCouponQuery;
import com.tianji.promotion.domain.vo.CouponVO;

public interface IUserCouponService extends IService<UserCoupon>
    {
        
        /**
         * 当前用户已领取优惠券的数量
         *
         * @param couponId 优惠券id
         * @return boolean
         */
        Integer numberOfCouponsClaimed(Long couponId);
        
        /**
         * 用户端领取优惠券
         *
         * @param id 优惠券id
         */
        void receiveCoupon(Long id);
        
        /**
         * 兑换码兑换优惠券
         *
         * @param code 兑换码
         */
        void exchangeCoupon(String code);
        
        /**
         * 检查和创造用户优惠券
         *
         * @param coupon    优惠券
         * @param userId    用户id
         * @param serialNum 序列号
         */
        void checkAndCreatUserCoupon(Coupon coupon, Long userId, Integer serialNum);
        
        /**
         * 分页查询我的优惠券
         *
         * @param query 查询条件
         * @return {@link PageDTO }<{@link CouponVO }>
         */
        PageDTO<CouponVO> queryMyCoupon(UserCouponQuery query);
    }
