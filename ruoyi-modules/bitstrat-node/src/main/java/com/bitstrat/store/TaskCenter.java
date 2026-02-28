package com.bitstrat.store;

import com.bitstrat.task.MarketTask;
import com.bitstrat.task.OrderBookTask;
import com.bitstrat.task.OrderTask;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 17:55
 * @Content
 */

public class TaskCenter {

    private final static ConcurrentHashMap<String, MarketTask> marketTaskMap = new ConcurrentHashMap<String, MarketTask>();
    private final static ConcurrentHashMap<String, OrderBookTask> orderBookTaskMap = new ConcurrentHashMap<String, OrderBookTask>();
    private final static ConcurrentHashMap<String, OrderTask> orderTaskMap = new ConcurrentHashMap<String, OrderTask>();

    public static ConcurrentHashMap<String, MarketTask> getMarketTaskMap() {
        return marketTaskMap;
    }
    public static ConcurrentHashMap<String, OrderBookTask> getOrderBookTaskMap() {
        return orderBookTaskMap;
    }
    public static ConcurrentHashMap<String, OrderTask> getOrderTaskMap() {
        return orderTaskMap;
    }
}
