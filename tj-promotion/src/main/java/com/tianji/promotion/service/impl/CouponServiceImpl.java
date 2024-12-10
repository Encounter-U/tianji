package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.promotion.domain.dto.CouponFormDTO;
import com.tianji.promotion.domain.dto.CouponIssueFormDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.CouponScope;
import com.tianji.promotion.domain.query.CouponQuery;
import com.tianji.promotion.domain.vo.CouponDetailVO;
import com.tianji.promotion.domain.vo.CouponPageVO;
import com.tianji.promotion.domain.vo.CouponScopeVO;
import com.tianji.promotion.domain.vo.CouponVO;
import com.tianji.promotion.enums.CouponStatus;
import com.tianji.promotion.enums.ObtainType;
import com.tianji.promotion.mapper.CouponMapper;
import com.tianji.promotion.service.ICouponScopeService;
import com.tianji.promotion.service.ICouponService;
import com.tianji.promotion.service.IExchangeCodeService;
import com.tianji.promotion.service.IUserCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements ICouponService
    {
        private final ICouponScopeService scopeService;
        private final IExchangeCodeService codeService;
        private final IUserCouponService userCouponService;
        
        /**
         * 添加优惠券
         *
         * @param couponFormDTO 优惠券表格 DTO
         */
        @Override
        @Transactional
        public void addCoupon(CouponFormDTO couponFormDTO)
            {
                //保存优惠券
                //转po
                Coupon coupon = BeanUtils.copyBean(couponFormDTO, Coupon.class);
                //保存
                save(coupon);
                
                //没有限定范围
                if (!coupon.getSpecific())
                    {
                        return;
                    }
                
                //有限定范围
                long couponId = coupon.getId();
                //保存优惠券范围
                List<Long> scopes = couponFormDTO.getScopes();
                if (CollUtils.isEmpty(scopes))
                    {
                        throw new BadRequestException("优惠券限定范围不能为空");
                    }
                //转换为po
                List<CouponScope> collect = scopes.stream()
                        .map(b -> new CouponScope().setBizId(b).setCouponId(couponId))
                        .collect(Collectors.toList());
                //保存
                scopeService.saveBatch(collect);
            }
        
        /**
         * 页面查询
         *
         * @param query 查询条件
         * @return {@link PageDTO }<{@link CouponPageVO }>
         */
        @Override
        public PageDTO<CouponPageVO> pageQuery(CouponQuery query)
            {
                //获取查询条件
                Integer type = query.getType();
                String name = query.getName();
                Integer status = query.getStatus();
                //开始查询
                Page<Coupon> page = lambdaQuery()
                        .eq(type != null, Coupon::getType, type)
                        .like(StringUtils.isNotBlank(name), Coupon::getName, name)
                        .eq(status != null, Coupon::getStatus, status)
                        .page(query.toMpPageDefaultSortByCreateTimeDesc());
                //没有数据
                if (CollUtils.isEmpty(page.getRecords()))
                    {
                        return PageDTO.empty(0L, 0L);
                    }
                //转vo
                List<CouponPageVO> collect = page.getRecords().stream()
                        .map(c -> BeanUtils.copyBean(c, CouponPageVO.class))
                        .collect(Collectors.toList());
                //封装数据返回
                return PageDTO.of(page, collect);
            }
        
        /**
         * 发行优惠券
         *
         * @param couponIssueFormDTO 优惠券发行表单 DTO
         */
        @Override
        public void issueCoupon(CouponIssueFormDTO couponIssueFormDTO)
            {
                //获取优惠券id
                Long id = couponIssueFormDTO.getId();
                //判断优惠券是否存在
                Coupon coupon = getById(id);
                if (coupon == null)
                    {
                        throw new BadRequestException("优惠券不存在");
                    }
                //判断优惠券状态 是否为待发放或暂停
                if (coupon.getStatus() != CouponStatus.DRAFT && coupon.getStatus() != CouponStatus.PAUSE)
                    {
                        throw new BadRequestException("优惠券状态不正确");
                    }
                //存在，发行优惠券
                Coupon update = BeanUtils.copyBean(couponIssueFormDTO, Coupon.class);
                
                //判断发行时间是否在现在之后
                LocalDateTime issueBeginTime = couponIssueFormDTO.getIssueBeginTime();
                boolean isBegin = issueBeginTime == null || !issueBeginTime.isAfter(LocalDateTime.now());
                
                //修改发行状态
                if (isBegin)
                    {
                        update.setStatus(CouponStatus.ISSUING);
                        update.setIssueBeginTime(LocalDateTime.now());
                    }
                else
                    {
                        update.setStatus(CouponStatus.UN_ISSUE);
                    }
                
                //开始发放
                updateById(update);
                
                //判断是否需要生成兑换码，优惠券类型必须是兑换码，优惠券状态必须是待发放
                if (coupon.getObtainWay() == ObtainType.ISSUE && coupon.getStatus() == CouponStatus.DRAFT)
                    {
                        coupon.setIssueEndTime(update.getIssueEndTime());
                        codeService.asyncGenerateCode(coupon);
                    }
            }
        
        /**
         * 修改优惠券
         *
         * @param couponFormDTO 优惠券表格 DTO
         */
        @Override
        @Transactional
        public void updateCoupon(CouponFormDTO couponFormDTO)
            {
                //获取优惠券id
                Long couponId = couponFormDTO.getId();
                //判断优惠券是否存在
                Coupon coupon = getById(couponId);
                if (coupon == null)
                    {
                        throw new BadRequestException("优惠券不存在");
                    }
                //判断优惠券状态 是否为待发放
                if (coupon.getStatus() != CouponStatus.DRAFT)
                    {
                        throw new BadRequestException("优惠券状态不正确");
                    }
                //修改优惠券
                Coupon update = BeanUtils.copyBean(couponFormDTO, Coupon.class);
                checkScopes(couponId);
                if (update.getSpecific())
                    {
                        //限定了适用范围
                        //保存新的限定范围
                        List<Long> scopeIds = couponFormDTO.getScopes();
                        if (CollUtils.isNotEmpty(scopeIds))
                            {
                                List<CouponScope> collect = scopeIds.stream()
                                        .map(b -> new CouponScope().setBizId(b).setCouponId(couponId))
                                        .collect(Collectors.toList());
                                scopeService.saveBatch(collect);
                            }
                    }
                
                //设置修改人
                update.setUpdater(UserContext.getUser());
                
                //修改
                updateById(update);
            }
        
        /**
         * 删除优惠券
         *
         * @param id 优惠券id
         */
        @Override
        public void deleteCoupon(Long id)
            {
                //判断优惠券是否存在
                Coupon coupon = getById(id);
                if (coupon == null)
                    {
                        throw new BadRequestException("优惠券不存在");
                    }
                //判断优惠券状态 是否为待发放
                if (coupon.getStatus() != CouponStatus.DRAFT)
                    {
                        throw new BadRequestException("优惠券状态不正确");
                    }
                //删除
                removeById(id);
                //删除限定范围
                checkScopes(id);
            }
        
        /**
         * 获取优惠券由id
         *
         * @param id 优惠券id
         * @return {@link CouponDetailVO }
         */
        @Override
        public CouponDetailVO getCouponById(Long id)
            {
                //查询优惠券
                Coupon coupon = getById(id);
                if (coupon == null)
                    {
                        return null;
                    }
                //转vo
                CouponDetailVO vo = BeanUtils.copyBean(coupon, CouponDetailVO.class);
                
                //判断是否有限定范围
                if (coupon.getSpecific())
                    {
                        List<CouponScopeVO> scopes = scopeService.listByCouponId(vo.getId());
                        vo.setScopes(scopes);
                    }
                
                //返回
                return vo;
            }
        
        /**
         * 暂停发行优惠券
         *
         * @param id id
         */
        @Override
        public void pauseIssueCoupon(Long id)
            {
                Coupon coupon = getById(id);
                if (coupon == null)
                    {
                        throw new BadRequestException("优惠券不存在");
                    }
                if (coupon.getStatus() != CouponStatus.ISSUING)
                    {
                        throw new BadRequestException("优惠券状态不正确");
                    }
                coupon.setStatus(CouponStatus.PAUSE);
                updateById(coupon);
            }
        
        /**
         * 查询发行中的优惠券
         *
         * @return {@link List }<{@link CouponVO }>
         */
        @Override
        public List<CouponVO> queryIssuingCoupon()
            {
                //查询发行中的优惠券
                List<Coupon> coupons = lambdaQuery()
                        .eq(Coupon::getStatus, CouponStatus.ISSUING)
                        .eq(Coupon::getObtainWay, ObtainType.PUBLIC)
                        .list();
                //没有数据
                if (CollUtils.isEmpty(coupons))
                    {
                        return CollUtils.emptyList();
                    }
                //转vo
                return coupons.stream()
                        .map(c ->
                            {
                                //查询已领取的数量
                                Integer count = userCouponService.numberOfCouponsClaimed(c.getId());
                                return BeanUtils.copyBean(c, CouponVO.class)
                                        .setAvailable(c.getTotalNum() > c.getIssueNum()
                                                && count < c.getUserLimit())
                                        .setReceived(count > 0);
                            })
                        .collect(Collectors.toList());
            }
        
        /**
         * 检查是否存在限定范围，存在则删除
         *
         * @param couponId 优惠券id
         */
        private void checkScopes(Long couponId)
            {
                //查询是否有原限定范围
                List<CouponScope> scopes = scopeService.lambdaQuery()
                        .eq(CouponScope::getCouponId, couponId)
                        .list();
                if (CollUtils.isNotEmpty(scopes))
                    {
                        //删除原有限定范围
                        scopeService.removeByIds(scopes.stream().map(CouponScope::getId).collect(Collectors.toList()));
                    }
            }
    }
