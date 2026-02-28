package com.bitstrat.ai.handler;

import com.bitstrat.ai.distuptor.MarketPriceDisruptor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/16 16:26
 * @Content
 */

public class MarketStore {
    /**
     * key exchange:symbol
     * 交易所:币对
     */
    final static ConcurrentHashMap<String, MarketPriceDisruptor> marketPriceStore = new ConcurrentHashMap<>();
    public MarketStore() {

    }

    public static MarketPriceDisruptor getDisruptorByExAndSymbol(String ex, String symbol) {
        String key = ex +":"+ symbol;
        //5*60*60*24 一天的数据量是 432000
        return marketPriceStore.computeIfAbsent(key, k -> new MarketPriceDisruptor(432000));
    }


}
