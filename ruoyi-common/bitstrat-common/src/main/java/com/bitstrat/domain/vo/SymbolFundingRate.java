package com.bitstrat.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/22 19:54
 * @Content
 */

@Data
public class SymbolFundingRate {
    /**
     * 资金费率
     */
    private BigDecimal fundingRate;
    /**
     * 下次资金结算时间戳
     */
    private Long nextFundingTime;
}
