package com.bitstrat.ai.domain;

import com.bitstrat.ai.distuptor.ABDisruptor;
import com.bitstrat.ai.distuptor.MarketPriceDisruptor;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/4 18:21
 * @Content
 */
@Data
public class ExtConfig {

    MarketPriceDisruptor marketPriceDisruptor;

    ABDisruptor abDisruptor;
}
