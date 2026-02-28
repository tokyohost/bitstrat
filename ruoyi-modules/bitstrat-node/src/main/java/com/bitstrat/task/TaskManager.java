package com.bitstrat.task;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 18:20
 * @Content
 */

@Component
public class TaskManager {
    List<MarketTask> tasks = new ArrayList<>();
    ConcurrentHashMap<String, MarketTask> marketTaskStrategy = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, OrderBookTask> orderBookTaskStrategy = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, OrderTask> orderTaskStrategy = new ConcurrentHashMap<>();


    public TaskManager(List<MarketTask> tasks,List<OrderBookTask> orderBookTasks,List<OrderTask> orderTasks) {
        this.tasks = tasks;
        for (MarketTask task : tasks) {
            marketTaskStrategy.put(task.getExchangeName(), task);
        }
        for (OrderBookTask task : orderBookTasks) {
            orderBookTaskStrategy.put(task.getExchangeName(), task);
        }
        for (OrderTask task : orderTasks) {
            orderTaskStrategy.put(task.getExchangeName(), task);
        }
    }

    public MarketTask getMarketTaskStrategy(String exchangeName) {
        return marketTaskStrategy.get(exchangeName);
    }
    public OrderBookTask getOrderBookTaskStrategy(String exchangeName) {
        return orderBookTaskStrategy.get(exchangeName);
    }

    public OrderTask getOrderTaskStrategy(String exchangeName) {
        return orderTaskStrategy.get(exchangeName);
    }
}
