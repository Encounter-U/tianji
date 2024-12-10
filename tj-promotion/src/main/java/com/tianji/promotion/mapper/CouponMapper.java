package com.tianji.promotion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.promotion.domain.po.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon>
    {
        /**
         * 增加发放数量
         *
         * @param id 优惠券id
         * @return int 影响行数
         */
        @Update("update coupon set issue_num = issue_num + 1 where id = #{id} and issue_num < total_num")
        int incrIssueNum(Long id);
    }