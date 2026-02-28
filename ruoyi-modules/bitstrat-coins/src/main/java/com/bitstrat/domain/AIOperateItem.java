// AIOperateItem.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain;
import java.math.BigDecimal;
import java.util.List;

import com.alibaba.fastjson2.annotation.JSONField;
import com.bitstrat.domain.vo.CoinsAiTaskVo;
import lombok.Data;

@Data
public class AIOperateItem {
    private BigDecimal leverage;
    private String symbol;
    @JSONField(name = "take_profit")
    private BigDecimal takeProfit;
    private BigDecimal size;
    @JSONField(name = "stop_loss")
    private BigDecimal stopLoss;
    private Reasoning reasoning;

    @JSONField(name = "invalidation_condition")
    private String invalidationCondition;
    private BigDecimal confidence;
    @JSONField(name = "risk_usd")
    private BigDecimal riskUsd;
    private String action;
    private Long userId;
    private Long taskId;

    private CoinsAiTaskVo taskVo;

    @JSONField(name = "effective_price_range")
    private EffectivePriceRange effectivePriceRange;

}

// Reasoning.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

