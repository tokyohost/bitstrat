package com.bitstrat.ai.distuptor;

import com.lmax.disruptor.EventFactory;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/16 15:19
 * @Content
 */

public class MarketPriceEventFactory implements EventFactory<MarketPriceEvent> {
    @Override
    public MarketPriceEvent newInstance() {
        return new MarketPriceEvent();
    }
}
