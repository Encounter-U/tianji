package com.tianji.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.promotion.domain.dto.CouponFormDTO;
import com.tianji.promotion.domain.dto.CouponIssueFormDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.query.CouponQuery;
import com.tianji.promotion.domain.vo.CouponDetailVO;
import com.tianji.promotion.domain.vo.CouponPageVO;

import javax.validation.Valid;

public interface ICouponService extends IService<Coupon>
    {
        /**
         * 添加优惠券
         *
         * @param couponFormDTO 优惠券表格 DTO
         */
        void addCoupon(CouponFormDTO couponFormDTO);
        
        /**
         * 页面查询
         *
         * @param query 查询
         * @return {@link PageDTO }<{@link CouponPageVO }>
         */
        PageDTO<CouponPageVO> pageQuery(CouponQuery query);
        
        /**
         * 发行优惠券
         *
         * @param couponIssueFormDTO 优惠券发行表单 DTO
         */
        void issueCoupon(CouponIssueFormDTO couponIssueFormDTO);
        
        /**
         * 修改优惠券
         *
         * @param couponFormDTO 优惠券表格 DTO
         */
        void updateCoupon(@Valid CouponFormDTO couponFormDTO);
        
        /**
         * 删除优惠券
         *
         * @param id 优惠券id
         */
        void deleteCoupon(Long id);
        
        /**
         * 获取优惠券由id
         *
         * @param id 优惠券id
         * @return {@link CouponDetailVO }
         */
        CouponDetailVO getCouponById(Long id);
        
        /**
         * 暂停发行优惠券
         *
         * @param id id
         */
        void pauseIssueCoupon(Long id);
    }
