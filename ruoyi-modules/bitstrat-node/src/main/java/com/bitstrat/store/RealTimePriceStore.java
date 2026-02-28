package com.bitstrat.store;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 22:05
 * @Content
 */

public class RealTimePriceStore {
    private final static ConcurrentHashMap<String, Double> realTimePrice = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, Double> getRealTimePrice() {
        return realTimePrice;
    }
}
