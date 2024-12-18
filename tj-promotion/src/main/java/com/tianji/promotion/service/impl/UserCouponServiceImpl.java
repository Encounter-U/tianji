package com.tianji.promotion.service.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.NumberUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.promotion.constants.PromotionConstants;
import com.tianji.promotion.domain.dto.UserCouponDTO;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements IUserCouponService
    {
        private final CouponMapper couponMapper;
        private final IExchangeCodeService codeService;
        private static final RedisScript<Long> RECEIVE_COUPON_SCRIPT;
        private static final RedisScript<String> EXCHANGE_COUPON_SCRIPT;

        static
            {
                RECEIVE_COUPON_SCRIPT = RedisScript.of(new ClassPathResource("lua/receive_coupon.lua"), Long.class);
                EXCHANGE_COUPON_SCRIPT = RedisScript.of(new ClassPathResource("lua/exchange_coupon.lua"), String.class);
            }

        private final StringRedisTemplate redisTemplate;
        private final RabbitMqHelper mqHelper;
        
        /**
         * 用户端领取优惠券
         *
         * @param couponId 优惠券id
         */
        @Override
        //使用LUA脚本后不再需要加锁
        //不必再加锁，以注解方式实现
        //@Lock(name = "lock:coupon:#{couponId}")
        //@Transactional
        public void receiveCoupon(Long couponId)
            {
                //获取当前用户
                Long userId = UserContext.getUser();
                //查询缓存中的优惠券信息
                /*Coupon coupon = queryCouponByCache(couponId);
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
                    }*/
                
                //执行脚本，判断结果
                //准备参数
                String key1 = PromotionConstants.COUPON_CACHE_KEY_PREFIX + couponId;
                String key2 = PromotionConstants.USER_COUPON_CACHE_KEY_PREFIX + couponId;
                //执行脚本
                Long r = redisTemplate.execute(RECEIVE_COUPON_SCRIPT, List.of(key1, key2), userId.toString());
                int result = NumberUtils.null2Zero(r).intValue();
                if (result != 0)
                    {
                        //结果大于0，说明出现异常
                        throw new BizIllegalException(PromotionConstants.RECEIVE_COUPON_ERROR_MSG[result - 1]);
                    }
                
                //领取优惠券
                //原本通过this调用，但是在非事务方法中调用事务方法会导致事务失效，所以通过暴露代理调用
                /*synchronized (userId.toString().intern())
                    {
                        //checkAndCreatUserCoupon(coupon, UserContext.getUser(), null);
                        IUserCouponService userCouponService = (IUserCouponService) AopContext.currentProxy();
                        userCouponService.checkAndCreatUserCoupon(coupon, userId, null);
                    }*/
                //创建锁对象
                /*IUserCouponService userCouponService = (IUserCouponService) AopContext.currentProxy();
                userCouponService.checkAndCreatUserCoupon(coupon, userId, null);*/
                
                //异步领券
                //查询领取数量
                /*String key = PromotionConstants.USER_COUPON_CACHE_KEY_PREFIX + couponId;
                Long count = redisTemplate.opsForHash().increment(key, userId.toString(), 1);
                //校验领取数量
                if (count > coupon.getUserLimit())
                    {
                        throw new BadRequestException("优惠券已达到领取上限");
                    }
                
                //扣减库存数量
                redisTemplate.opsForHash()
                        .increment(PromotionConstants.COUPON_CACHE_KEY_PREFIX + couponId,
                                "totalNum", -1);*/
                
                //发送MQ消息
                UserCouponDTO uc = new UserCouponDTO();
                uc.setCouponId(couponId);
                uc.setUserId(userId);
                mqHelper.send(MqConstants.Exchange.PROMOTION_EXCHANGE,
                        MqConstants.Key.COUPON_RECEIVE, uc);
            }
        
        /**
         * 兑换码兑换优惠券
         *
         * @param code 兑换码
         */
        @Override
        //使用LUA脚本后不再需要加锁
        //不必再加锁，以注解方式实现
        //@Lock(name = "lock:coupon:#{T(com.tianji.common.utils.UserContext).getUser()}")
        //@Transactional
        public void exchangeCoupon(String code)
            {
                //解析兑换码，兑换码id
                long serialNum = CodeUtil.parseCode(code);
                Long userId = UserContext.getUser();
                
                //执行脚本
                String result = redisTemplate.execute(EXCHANGE_COUPON_SCRIPT,
                        List.of(PromotionConstants.COUPON_CODE_MAP_KEY, PromotionConstants.COUPON_RANGE_KEY),
                        String.valueOf(serialNum), String.valueOf(serialNum + 5000), userId.toString());
                
                long r = NumberUtils.parseLong(result);
                log.info("兑换结果：{}", r);
                if (r < 10)
                    {
                        //异常结果应在 1 ~ 5 之间
                        throw new BizIllegalException(PromotionConstants.EXCHANGE_COUPON_ERROR_MSG[(int) (r - 1)]);
                    }
                
                //校验是否已兑换，直接执行setbit操作，通过返回值确定是否兑换
                /*boolean exchanged = codeService.updateExchangeMark(serialNum, true);
                if (exchanged)
                    {
                        throw new BizIllegalException("兑换码已被兑换");
                    }
                //开始兑换
                try
                    {
                        //兑换码是否存在
                        Long couponId = codeService.exchangeTargetId(serialNum);
                        if (couponId == null)
                            {
                                throw new BizIllegalException("兑换码不存在");
                            }
                        //兑换码是否过期
                        *//*if (exchangeCode.getExpiredTime().isBefore(LocalDateTime.now()))
                            {
                                throw new BizIllegalException("兑换码已过期");
                            }*//*
                        //通过缓存获取优惠券信息
                        Coupon coupon = queryCouponByCache(couponId);
                        if (coupon == null)
                            {
                                throw new BizIllegalException("优惠券不存在");
                            }
                        //判断是否过期
                        LocalDateTime now = LocalDateTime.now();
                        if (coupon.getIssueBeginTime().isAfter(now) || coupon.getIssueEndTime().isBefore(now))
                            {
                                throw new BizIllegalException("优惠券不在可领取时间范围内");
                            }
                        
                        //校验并领取优惠券，同时更新优惠券状态
                        Long userId = UserContext.getUser();
                        String key = PromotionConstants.USER_COUPON_CACHE_KEY_PREFIX + coupon.getId();
                        Long count = redisTemplate.opsForHash()
                                .increment(key, userId.toString(), 1);
                        
                        //校验领取数量
                        if (coupon.getUserLimit() != 0 && count > coupon.getUserLimit())
                            {
                                throw new BizIllegalException("优惠券已达到领取上限");
                            }*/
                
                //用户优惠券信息
                UserCouponDTO userCouponDTO = new UserCouponDTO()
                        .setCouponId(r)
                        .setUserId(userId)
                        .setSerialNum((int) serialNum);
                
                //发送MQ消息
                mqHelper.send(MqConstants.Exchange.PROMOTION_EXCHANGE,
                        MqConstants.Key.COUPON_RECEIVE, userCouponDTO);
                        
               /*     }
                catch (Exception e)
                    {
                        //兑换失败，回滚
                        codeService.updateExchangeMark(serialNum, false);
                        throw e;
                    }*/
            }
        
        /**
         * 检查和创建用户优惠券记录
         *
         * @param userCouponDTO 用户优惠券DTO
         */
        @Override
        @Transactional
        public void checkAndCreatUserCoupon(UserCouponDTO userCouponDTO)
            {
                //获取用户优惠券信息
                Long couponId = userCouponDTO.getCouponId();
                Long userId = userCouponDTO.getUserId();
                Integer serialNum = userCouponDTO.getSerialNum();
                
                //判断优惠券是否存在
                Coupon coupon = couponMapper.selectById(couponId);
                if (coupon == null)
                    {
                        throw new BizIllegalException("优惠券不存在");
                    }
                
                //封装用户领取优惠券记录信息
                saveUserCoupon(coupon, userId);
                //更新优惠券发放数量
                int num = couponMapper.incrIssueNum(coupon.getId());
                if (num == 0)
                    {
                        throw new BizIllegalException("优惠券库存不足");
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
         * 查询优惠券缓存
         *
         * @param couponId 优惠券id
         * @return {@link Coupon }
         */
        private Coupon queryCouponByCache(Long couponId)
            {
                //查询缓存中的优惠券信息
                String key = PromotionConstants.COUPON_CACHE_KEY_PREFIX + couponId;
                Map<Object, Object> couponMap = redisTemplate.opsForHash().entries(key);
                if (CollUtils.isEmpty(couponMap))
                    {
                        return null;
                    }
                
                //反序列化
                return BeanUtils.mapToBean(couponMap, Coupon.class, false, CopyOptions.create());
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
