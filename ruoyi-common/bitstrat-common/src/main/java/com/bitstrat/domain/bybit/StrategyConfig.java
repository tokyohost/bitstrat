package com.bitstrat.domain.bybit;

import lombok.Data;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/2 17:20
 * @Content
 */

@Data
public class StrategyConfig {
    private List<String> order;
    private List<StrategySell> sell;

}
