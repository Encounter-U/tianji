package com.tianji.promotion.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Encounter
 * @date 2024/12/08 22:34<br/>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "兑换码信息")
public class ExchangeCodeVO
    {
        //兑换码id
        private Long id;
        //兑换码
        private String code;
    }
