package com.tianji.promotion.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.promotion.domain.query.CodeQuery;
import com.tianji.promotion.domain.vo.ExchangeCodeVO;
import com.tianji.promotion.service.IExchangeCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 兑换码(tj_promotion.exchange_code)表控制层
 *
 * @author Encounter
 * @date 2024-12-04
 */
@RestController
@RequestMapping("/codes")
@RequiredArgsConstructor
@Tag(name = "兑换码相关接口")
public class ExchangeCodeController
    {
        //服务对象
        private final IExchangeCodeService codeService;
        
        /**
         * 分页查询兑换码
         *
         * @param query 查询条件
         * @return {@link PageDTO }<{@link ExchangeCodeVO }>
         */
        @GetMapping("/page")
        @Operation(summary = "分页查询兑换码")
        public PageDTO<ExchangeCodeVO> pageQuery(CodeQuery query)
            {
                return codeService.pageQuery(query);
            }
    }
