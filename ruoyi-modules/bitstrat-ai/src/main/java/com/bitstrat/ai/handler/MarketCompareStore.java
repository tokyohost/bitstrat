package com.bitstrat.ai.handler;

import com.bitstrat.ai.distuptor.MarketPriceDisruptor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/16 16:26
 * @Content
 */

public class MarketCompareStore {
    /**
     * key exchange:symbol
     * 交易所:币对
     */
    final static ConcurrentHashMap<String, MarketPriceDisruptor> marketPriceStore = new ConcurrentHashMap<>();
    public MarketCompareStore() {

    }

    public static MarketPriceDisruptor getDisruptorByExAndSymbol(String clientId,String symbol, String exchange,String type) {
        String key = clientId + ":"+exchange + ":" + symbol + ":" + type;
        //5*60*60*24 一天的数据量是 432000
        return marketPriceStore.computeIfAbsent(key, k -> new MarketPriceDisruptor(256));
    }


}
