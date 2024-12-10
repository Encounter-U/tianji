package com.tianji.promotion.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.promotion.domain.dto.CouponFormDTO;
import com.tianji.promotion.domain.dto.CouponIssueFormDTO;
import com.tianji.promotion.domain.query.CouponQuery;
import com.tianji.promotion.domain.vo.CouponDetailVO;
import com.tianji.promotion.domain.vo.CouponPageVO;
import com.tianji.promotion.domain.vo.CouponVO;
import com.tianji.promotion.service.ICouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 优惠券的规则信息(tj_promotion.coupon)表控制层
 *
 * @author Encounter
 * @date 2024-12-04
 */
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
@Tag(name = "优惠券相关接口")
public class CouponController
    {
        //服务对象
        private final ICouponService couponService;
        
        /**
         * 添加优惠券
         *
         * @param couponFormDTO 优惠券表单 DTO
         */
        @PostMapping
        @Operation(summary = "创建优惠券")
        public void addCoupon(@RequestBody @Valid CouponFormDTO couponFormDTO)
            {
                couponService.addCoupon(couponFormDTO);
            }
        
        /**
         * 页面查询
         *
         * @param query 查询条件
         * @return {@link PageDTO }<{@link CouponPageVO }>
         */
        @GetMapping("/page")
        @Operation(summary = "分页查询优惠券")
        public PageDTO<CouponPageVO> pageQuery(CouponQuery query)
            {
                return couponService.pageQuery(query);
            }
        
        /**
         * 发行优惠券
         *
         * @param id                 优惠券id
         * @param couponIssueFormDTO 优惠券发行表单 DTO
         */
        @PutMapping("/{id}/issue")
        @Operation(summary = "发放优惠券")
        public void issueCoupon(@PathVariable Long id,
                                @RequestBody @Valid CouponIssueFormDTO couponIssueFormDTO)
            {
                couponIssueFormDTO.setId(id);
                couponService.issueCoupon(couponIssueFormDTO);
            }
        
        /**
         * 修改优惠券
         *
         * @param id            优惠券id
         * @param couponFormDTO 优惠券表格 DTO
         */
        @PutMapping("/{id}")
        @Operation(summary = "修改优惠券")
        public void updateCoupon(@PathVariable Long id, @RequestBody @Valid CouponFormDTO couponFormDTO)
            {
                couponFormDTO.setId(id);
                couponService.updateCoupon(couponFormDTO);
            }
        
        /**
         * 删除优惠券
         *
         * @param id 优惠券id
         */
        @DeleteMapping("/{id}")
        @Operation(summary = "删除优惠券")
        public void deleteCoupon(@PathVariable Long id)
            {
                couponService.deleteCoupon(id);
            }
        
        /**
         * 根据id获取优惠券信息
         *
         * @param id 优惠券id
         * @return {@link CouponDetailVO }
         */
        @GetMapping("/{id}")
        @Operation(summary = "获取优惠券详情")
        public CouponDetailVO getCouponById(@PathVariable Long id)
            {
                return couponService.getCouponById(id);
            }
        
        /**
         * 暂停发行优惠券
         *
         * @param id id
         */
        @PutMapping("/{id}/pause")
        @Operation(summary = "暂停发放优惠券")
        public void pauseIssueCoupon(@PathVariable Long id)
            {
                couponService.pauseIssueCoupon(id);
            }
        
        /**
         * 查询发行中的优惠券
         *
         * @return {@link List }<{@link CouponVO }>
         */
        @GetMapping("/list")
        @Operation(summary = "查询发放中的优惠券")
        public List<CouponVO> queryIssuingCoupon()
            {
                return couponService.queryIssuingCoupon();
            }
    }
