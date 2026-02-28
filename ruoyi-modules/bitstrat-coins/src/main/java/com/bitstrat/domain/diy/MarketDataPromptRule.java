package com.bitstrat.domain.diy;

import com.bitstrat.domain.MarketData;
import lombok.Data;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/22 17:38
 * @Content
 */
@Data
public class MarketDataPromptRule {
    private String shortPrompt;
    private String longPrompt;
    private String middlePrompt;
    private String symbol;
    private MarketData marketData;

    private List<ExtConfigItem> extConfigItems;
}
