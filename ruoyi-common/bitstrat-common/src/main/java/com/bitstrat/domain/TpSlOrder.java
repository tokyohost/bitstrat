package com.bitstrat.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/10/27 17:18
 * @Content
 */

@Data
public class TpSlOrder {

    private String symbol;
    private BigDecimal takeProfitPrice;
    private BigDecimal stopLossPrice;
}
