package com.bitstrat.ai.distuptor;

import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/16 15:19
 * @Content
 */

@Slf4j
public class MarketPriceEventHandler implements EventHandler<MarketPriceEvent> {
    @Override
    public void onEvent(MarketPriceEvent event, long sequence, boolean endOfBatch) {
        // 实际处理逻辑（此处不处理）
//        log.info("Market Price Event: {}", event.getMarketPrice().toString());
    }
}
