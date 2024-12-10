package com.tianji.promotion.handler;

import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.sms.SmsInfoDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.utils.CollUtils;
import com.tianji.promotion.domain.po.UserCoupon;
import com.tianji.promotion.enums.UserCouponStatus;
import com.tianji.promotion.service.ICouponService;
import com.tianji.promotion.service.IUserCouponService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Encounter
 * @date 2024/12/10 17:56<br/>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserCouponExpiredHandler
    {
        private final IUserCouponService userCouponService;
        private final UserClient userClient;
        private final ICouponService couponService;
        private final RabbitMqHelper mqHelper;
        
        /**
         * 优惠券即将过期通知
         */
        @XxlJob("notificationOfExpiredCoupons")
        public void notificationOfExpiredCoupons()
            {
                log.info("开始检查是否有即将过期的优惠券");
                //查询所有发放中的还有3天过期的优惠券
                List<UserCoupon> coupons = userCouponService.lambdaQuery()
                        .lt(UserCoupon::getTermEndTime, LocalDateTime.now().plusDays(3))
                        .eq(UserCoupon::getStatus, UserCouponStatus.UNUSED)
                        .list();
                if (CollUtils.isEmpty(coupons))
                    {
                        //没有即将过期的优惠券，直接结束
                        log.info("没有即将过期的优惠券");
                        return;
                    }
                
                //获取用户id
                List<Long> userIds = coupons.stream().map(UserCoupon::getUserId).distinct().collect(Collectors.toList());
                //获取用户信息
                Map<Long, UserDTO> userDTOMap = userClient.queryUserByIds(userIds)
                        .stream()
                        .collect(Collectors.toMap(UserDTO::getId, u -> u));
                //获取用户手机号，确保获取到的用户手机号与userIds顺序一致
                List<String> phones = userIds.stream()
                        .map(userDTOMap::get)
                        .map(UserDTO::getCellPhone)
                        .collect(Collectors.toList());
                
                //TODO Encounter 2024/12/10 18:11 即将过期优惠券短信通知，已完成模板代码编写，待写输入真实模板参数
                /*
                 * 发送短信，通知用户优惠券即将过期
                 * 假定消息模板为SMS_123456，且没有模板参数
                 *
                 * 假设消息模板内容为：
                 * 尊敬的用户，您有优惠券即将过期，请尽快使用以免造成损失。
                 */
                //封装短信模板信息
                SmsInfoDTO smsInfoDTO = new SmsInfoDTO();
                //设置手机号
                smsInfoDTO.setPhones(phones);
                //设置模板代码
                smsInfoDTO.setTemplateCode("SMS_123456");
                //没有模板参数时，直接发送Mq消息
                mqHelper.send(MqConstants.Exchange.SMS_EXCHANGE, MqConstants.Key.SMS_MESSAGE, smsInfoDTO);
                log.info("即将过期的优惠券通知短信发送成功，共有{}条", coupons.size());
                
                /*
                 * 发送短信，通知用户优惠券即将过期
                 * 假定消息模板为SMS_123456，且模板参数有一个${couponName}，值为优惠券名称
                 *
                 * 假设消息模板内容为：
                 * 尊敬的用户，您有优惠券即还有3天将过期，优惠券名称为：${couponName}，请尽快使用以免造成损失。
                 */
                //获取优惠券id
                /*List<Long> couponIds = coupons.stream().map(UserCoupon::getCouponId).distinct().collect(Collectors.toList());
                //获取优惠券名称，确保获取到的优惠券名称与couponIds顺序一致，重复的优惠券名称拼接在一起用逗号分隔
                Map<Long, String> couponNameMap = couponService.lambdaQuery()
                        .in(Coupon::getId, couponIds)
                        .list()
                        .stream()
                        .collect(Collectors.toMap(Coupon::getId,
                                Coupon::getName,
                                //当key冲突时，将新值与旧值拼接
                                (existV, newV) -> existV + "，" + newV));*/
                
                /*Map<String, String> templateParams = new HashMap<>();
                //封装短信模板信息
                SmsInfoDTO smsInfoDTO = new SmsInfoDTO();
                //设置手机号
                smsInfoDTO.setPhones(phones);
                //设置模板代码
                smsInfoDTO.setTemplateCode("SMS_123456");
                //设置模板参数（当有模板参数时）
                for (UserCoupon coupon : coupons)
                    {
                        //每次循环都要清空模板参数
                        templateParams.put("couponName", couponNameMap.get(coupon.getCouponId()));
                        smsInfoDTO.setTemplateParams(templateParams);
                        //发送MQ短信
                        mqHelper.send(MqConstants.Exchange.SMS_EXCHANGE, MqConstants.Key.SMS_MESSAGE, smsInfoDTO);
                    }
                //没有模板参数时，直接发送Mq消息
                log.info("即将过期的优惠券通知短信发送成功，共有{}条", coupons.size());*/
            }
    }
