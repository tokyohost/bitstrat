package com.bitstrat.domain;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/11/3 9:19
 * @Content
 */

@Data
public class AISaveOperatePathData {
    private String symbol;
    @JSONField(name = "invalidation_condition")
    private String invalidationCondition;
    private BigDecimal confidence;
    @JSONField(name = "risk_usd")
    private BigDecimal riskUsd;

    private String positionId;
}
