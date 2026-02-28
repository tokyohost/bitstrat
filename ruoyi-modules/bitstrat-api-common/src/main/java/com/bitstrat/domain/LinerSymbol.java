package com.bitstrat.domain;

import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/11 17:19
 * @Content
 */

@Data
public class LinerSymbol {
    String symbol;
    String coin;
    /**
     * 资金费时长 小时
     */
    Integer fundingInterval;

}
