package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.utils.CollUtils;
import com.tianji.promotion.constants.PromotionConstants;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.ExchangeCode;
import com.tianji.promotion.domain.query.CodeQuery;
import com.tianji.promotion.domain.vo.ExchangeCodeVO;
import com.tianji.promotion.mapper.ExchangeCodeMapper;
import com.tianji.promotion.service.IExchangeCodeService;
import com.tianji.promotion.utils.CodeUtil;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExchangeCodeServiceImpl extends ServiceImpl<ExchangeCodeMapper, ExchangeCode> implements IExchangeCodeService
    {
        private final StringRedisTemplate redisTemplate;
        private final BoundValueOperations<String, String> serialOps;
        
        public ExchangeCodeServiceImpl(StringRedisTemplate redisTemplate)
            {
                this.redisTemplate = redisTemplate;
                this.serialOps = redisTemplate.boundValueOps(PromotionConstants.COUPON_CODE_SERIAL_KEY);
            }
        
        /**
         * 异步生成兑换码
         *
         * @param coupon 优惠券信息
         */
        @Override
        @Async("generateExchangeCodeExecutor")
        public void asyncGenerateCode(Coupon coupon)
            {
                //发放数量
                Integer totalNum = coupon.getTotalNum();
                //获取redis的自增序列号
                Long serial = serialOps.increment(totalNum);
                if (serial == null)
                    {
                        return;
                    }
                //最大序列号
                int maxSerialNum = serial.intValue();
                List<ExchangeCode> codes = new ArrayList<>(totalNum);
                for (int serialNum = maxSerialNum - totalNum + 1; serialNum <= maxSerialNum; serialNum++)
                    {
                        //生成兑换码
                        String code = CodeUtil.generateCode(serialNum, coupon.getId());
                        //保存兑换码
                        ExchangeCode exchangeCode = new ExchangeCode();
                        exchangeCode.setCode(code);
                        exchangeCode.setId(serialNum);
                        exchangeCode.setExchangeTargetId(coupon.getId());
                        exchangeCode.setExpiredTime(coupon.getIssueEndTime());
                        //添加到集合
                        codes.add(exchangeCode);
                    }
                
                //批量保存
                saveBatch(codes);
                
                //写入redis
                redisTemplate.opsForZSet().add(PromotionConstants.COUPON_CODE_SERIAL_KEY,
                        coupon.getId().toString(), maxSerialNum);
            }
        
        /**
         * 分页查询兑换码
         *
         * @param query 查询条件
         * @return {@link PageDTO }<{@link ExchangeCodeVO }>
         */
        @Override
        public PageDTO<ExchangeCodeVO> pageQuery(CodeQuery query)
            {
                //优惠券id
                Long couponId = query.getCouponId();
                //优惠券状态
                Integer status = query.getStatus();
                //分页查询
                Page<ExchangeCode> page = lambdaQuery()
                        .eq(ExchangeCode::getExchangeTargetId, couponId)
                        .eq(ExchangeCode::getStatus, status)
                        .page(query.toMpPage());
                if (CollUtils.isEmpty(page.getRecords()))
                    {
                        return PageDTO.empty(0L, 0L);
                    }
                
                //转换
                List<ExchangeCodeVO> vos = page.getRecords().stream()
                        .map(ec -> new ExchangeCodeVO()
                                .setId(Long.valueOf(ec.getId()))
                                .setCode(ec.getCode()))
                        .collect(Collectors.toList());
                
                //返回
                return PageDTO.of(page, vos);
            }
    }
