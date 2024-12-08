package com.tianji.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.promotion.domain.po.CouponScope;
import com.tianji.promotion.domain.vo.CouponScopeVO;

import java.util.List;

public interface ICouponScopeService extends IService<CouponScope>
    {
        /**
         * 根据优惠券id查询分类范围信息
         *
         * @param id 优惠券id
         * @return {@link List }<{@link CouponScopeVO }>
         */
        List<CouponScopeVO> listByCouponId(Long id);
    }
