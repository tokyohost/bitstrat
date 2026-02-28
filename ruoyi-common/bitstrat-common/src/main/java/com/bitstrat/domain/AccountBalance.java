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
public class AccountBalance {
    private String symbol;
    private BigDecimal balance;
    /**
     * 类似净值 统计时建议使用这个值
     */
    private BigDecimal equity;
    private BigDecimal cashBalance;
    private BigDecimal usdtBalance;
    private BigDecimal freeBalance;

    /**
     * 以下是api 相关
     */
    private String apiName;
    private Long apiId;

}
