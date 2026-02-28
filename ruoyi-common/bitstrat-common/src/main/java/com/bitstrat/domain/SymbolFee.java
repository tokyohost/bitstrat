package com.bitstrat.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/17 14:47
 * @Content
 */

@Data
public class SymbolFee {
    public String symbol;
    //合约吃单手续费
    public BigDecimal linerTakerFeeRate;
    //合约挂单手续费
    public BigDecimal linerMakerFeeRate;
    //现货吃单手续费
    public BigDecimal sportTakerFeeRate;
    //现货挂单手续费
    public BigDecimal sportMakerFeeRate;
//    public BigDecimal fee;

}
