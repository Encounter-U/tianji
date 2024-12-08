package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.cache.CategoryCache;
import com.tianji.api.client.course.CourseClient;
import com.tianji.common.utils.CollUtils;
import com.tianji.promotion.domain.po.CouponScope;
import com.tianji.promotion.domain.vo.CouponScopeVO;
import com.tianji.promotion.mapper.CouponScopeMapper;
import com.tianji.promotion.service.ICouponScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponScopeServiceImpl extends ServiceImpl<CouponScopeMapper, CouponScope> implements ICouponScopeService
    {
        private final CategoryCache categoryCache;
        private final CourseClient courseClient;
        
        /**
         * 根据优惠券id查询分类范围信息
         *
         * @param id 优惠券id
         * @return {@link List }<{@link CouponScopeVO }>
         */
        @Override
        public List<CouponScopeVO> listByCouponId(Long id)
            {
                //查询限定范围
                List<CouponScope> scopes = lambdaQuery()
                        .eq(CouponScope::getCouponId, id)
                        .list();
                
                //如果为空返回空集合
                if (CollUtils.isEmpty(scopes))
                    {
                        return CollUtils.emptyList();
                    }
                
                //封装返回数据
                return scopes.stream()
                        .map(cs -> new CouponScopeVO()
                                .setId(cs.getId())
                                .setName(getNameByTypeAndBizId(cs.getType(), cs.getBizId())))
                        .collect(Collectors.toList());
                
            }
        
        /**
         * 获取名字由优惠券类型和业务id
         *
         * @param type  类型
         * @param bizId 业务id
         * @return {@link String }
         */
        private String getNameByTypeAndBizId(Integer type, Long bizId)
            {
                switch (type)
                    {
                        case 1:
                            return categoryCache.getNameByLv3Id(bizId);
                        case 2:
                            return courseClient.getCourseInfoById(bizId, false, false).getName();
                        default:
                            return null;
                    }
            }
    }
