package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.ExchangeCode;
import com.tianji.promotion.domain.po.UserCoupon;
import com.tianji.promotion.domain.query.UserCouponQuery;
import com.tianji.promotion.domain.vo.CouponVO;
import com.tianji.promotion.enums.ExchangeCodeStatus;
import com.tianji.promotion.mapper.CouponMapper;
import com.tianji.promotion.mapper.UserCouponMapper;
import com.tianji.promotion.service.IExchangeCodeService;
import com.tianji.promotion.service.IUserCouponService;
import com.tianji.promotion.utils.CodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements IUserCouponService
    {
        private final CouponMapper couponMapper;
        private final IExchangeCodeService codeService;
        
        /**
         * 当前用户是否已领取优惠券
         *
         * @param couponId 优惠券id
         * @return boolean
         */
        @Override
        public Integer numberOfCouponsClaimed(Long couponId)
            {
                // 获取当前用户
                Long userId = UserContext.getUser();
                // 查询是否已领取
                return lambdaQuery()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getCouponId, couponId)
                        .count();
            }
        
        /**
         * 用户端领取优惠券
         *
         * @param id 优惠券id
         */
        @Override
        //@Transactional
        public void receiveCoupon(Long id)
            {
                //获取当前用户
                Long userId = UserContext.getUser();
                
                Coupon coupon = couponMapper.selectById(id);
                //判断优惠券是否存在
                if (coupon == null)
                    {
                        throw new RuntimeException("优惠券不存在");
                    }
                //判断优惠券是否还能继续领取
                if (coupon.getTotalNum() <= coupon.getIssueNum())
                    {
                        throw new BadRequestException("优惠券库存不足");
                    }
                LocalDateTime now = LocalDateTime.now();
                if (coupon.getIssueBeginTime().isAfter(now) || coupon.getIssueEndTime().isBefore(now))
                    {
                        throw new BadRequestException("优惠券不在可领取时间范围内");
                    }
                //领取优惠券
                //原本通过this调用，但是在非事务方法中调用事务方法会导致事务失效，所以通过暴露代理调用
                synchronized (userId.toString().intern())
                    {
                        //checkAndCreatUserCoupon(coupon, UserContext.getUser(), null);
                        IUserCouponService userCouponService = (IUserCouponService) AopContext.currentProxy();
                        userCouponService.checkAndCreatUserCoupon(coupon, userId, null);
                    }
            }
        
        /**
         * 兑换码兑换优惠券
         *
         * @param code 兑换码
         */
        @Override
        //@Transactional
        public void exchangeCoupon(String code)
            {
                //解析兑换码，兑换码id
                long serialNum = CodeUtil.parseCode(code);
                //校验是否已兑换，直接执行setbit操作，通过返回值确定是否兑换
                boolean exchanged = codeService.updateExchangeMark(serialNum, true);
                if (exchanged)
                    {
                        throw new BizIllegalException("兑换码已被兑换");
                    }
                //开始兑换
                try
                    {
                        //兑换码是否存在
                        ExchangeCode exchangeCode = codeService.getById(serialNum);
                        if (exchangeCode == null)
                            {
                                throw new BizIllegalException("兑换码不存在");
                            }
                        //兑换码是否过期
                        if (exchangeCode.getExpiredTime().isBefore(LocalDateTime.now()))
                            {
                                throw new BizIllegalException("兑换码已过期");
                            }
                        //获取优惠券信息
                        Coupon coupon = couponMapper.selectById(exchangeCode.getExchangeTargetId());
                        //校验并领取优惠券，同时更新优惠券状态
                        //原本通过this调用，但是在非事务方法中调用事务方法会导致事务失效，所以通过暴露代理调用
                        Long userId = UserContext.getUser();
                        synchronized (userId.toString().intern())
                            {
                                //checkAndCreatUserCoupon(coupon, UserContext.getUser(), (int) serialNum);
                                IUserCouponService userCouponService = (IUserCouponService) AopContext.currentProxy();
                                userCouponService.checkAndCreatUserCoupon(coupon, userId, (int) serialNum);
                            }
                    }
                catch (Exception e)
                    {
                        //兑换失败，回滚
                        codeService.updateExchangeMark(serialNum, false);
                        throw e;
                    }
            }
        
        /**
         * 检查和创建用户优惠券记录
         *
         * @param coupon    优惠券
         * @param userId    用户
         * @param serialNum 序列号
         */
        @Override
        @Transactional
        public void checkAndCreatUserCoupon(Coupon coupon, Long userId, Integer serialNum)
            {
                Integer count = numberOfCouponsClaimed(coupon.getId());
                if (coupon.getUserLimit() != 0 && count >= coupon.getUserLimit())
                    {
                        throw new BadRequestException("优惠券已达到领取上限");
                    }
                //封装用户领取优惠券记录信息
                saveUserCoupon(coupon, userId);
                //更新优惠券发放数量
                int num = couponMapper.incrIssueNum(coupon.getId());
                if (num == 0)
                    {
                        throw new RuntimeException("优惠券库存不足");
                    }
                //更新兑换码状态
                if (serialNum != null)
                    {
                        codeService.lambdaUpdate()
                                .eq(ExchangeCode::getId, serialNum)
                                .set(ExchangeCode::getStatus, ExchangeCodeStatus.USED)
                                .set(ExchangeCode::getUserId, userId)
                                .update();
                    }
            }
        
        /**
         * 分页查询我的优惠券
         *
         * @param query 查询条件
         * @return {@link PageDTO }<{@link CouponVO }>
         */
        @Override
        public PageDTO<CouponVO> queryMyCoupon(UserCouponQuery query)
            {
                //分页查询用户优惠券记录
                Page<UserCoupon> page = lambdaQuery()
                        .eq(UserCoupon::getUserId, UserContext.getUser())
                        .eq(UserCoupon::getStatus, query.getStatus())
                        .page(query.toMpPage());
                //判断是否有数据
                List<UserCoupon> records = page.getRecords();
                if (CollUtils.isEmpty(records))
                    {
                        return PageDTO.empty(page);
                    }
                //封装用户优惠券信息
                List<CouponVO> collect = records.stream().map(
                        u ->
                            {
                                Coupon coupon = couponMapper.selectById(u.getCouponId());
                                return BeanUtils.copyBean(u, CouponVO.class)
                                        .setName(coupon.getName())
                                        .setSpecific(coupon.getSpecific())
                                        .setDiscountType(coupon.getDiscountType())
                                        .setThresholdAmount(coupon.getThresholdAmount())
                                        .setDiscountValue(coupon.getDiscountValue())
                                        .setMaxDiscountAmount(coupon.getMaxDiscountAmount())
                                        .setTermDays(coupon.getTermDays())
                                        .setTermEndTime(coupon.getTermEndTime());
                            }).collect(Collectors.toList());
                
                //返回分页数据
                return PageDTO.of(page, collect);
            }
        
        /**
         * 保存用户优惠券记录
         *
         * @param coupon 优惠券
         * @param userId 用户id
         */
        private void saveUserCoupon(Coupon coupon, Long userId)
            {
                //获取当前时间
                LocalDateTime now = LocalDateTime.now();
                //封装用户领取优惠券记录信息
                UserCoupon userCoupon = new UserCoupon();
                userCoupon.setUserId(userId);
                userCoupon.setCouponId(coupon.getId());
                if (coupon.getTermDays() == 0)
                    {
                        //有指定过期日期
                        userCoupon.setTermBeginTime(coupon.getTermBeginTime());
                        userCoupon.setTermEndTime(coupon.getTermEndTime());
                    }
                else
                    {
                        //自领取之日指定期限过期
                        userCoupon.setTermBeginTime(now);
                        userCoupon.setTermEndTime(now.plusDays(coupon.getTermDays()));
                    }
                //插入用户领取优惠券记录
                save(userCoupon);
            }
        
    }
