package com.bitstrat.domain.wsdomain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/9 21:15
 * @Content
 */

@Data
public class FeeDetail {
    private String feeCoin;
    private BigDecimal fee;
}
