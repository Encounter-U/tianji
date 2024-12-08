package com.tianji.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.ExchangeCode;
import com.tianji.promotion.domain.query.CodeQuery;
import com.tianji.promotion.domain.vo.ExchangeCodeVO;

public interface IExchangeCodeService extends IService<ExchangeCode>
    {
        /**
         * 异步生成兑换码
         *
         * @param coupon 优惠券信息
         */
        void asyncGenerateCode(Coupon coupon);
        
        /**
         * 分页查询兑换码
         *
         * @param query 查询条件
         * @return {@link PageDTO }<{@link ExchangeCodeVO }>
         */
        PageDTO<ExchangeCodeVO> pageQuery(CodeQuery query);
    }
