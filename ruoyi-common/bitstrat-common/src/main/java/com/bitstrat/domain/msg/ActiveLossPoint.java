package com.bitstrat.domain.msg;

import com.bitstrat.domain.bybit.ByBitAccount;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 11:07
 * @Content 已启用的滑点
 */
@Data
public class ActiveLossPoint {
    Long id;
    String nodeName;
    String exchangeName;
    String symbol;
     BigDecimal triggerPrice1;
     BigDecimal triggerPrice2;
     BigDecimal stopLossCalcLimit;
    ByBitAccount account;

    BigDecimal price;
    /**
     * 回撤率
     */
    BigDecimal retread;

    /**
     * 数量
     */
    BigDecimal quantity;


}
