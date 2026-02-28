package com.bitstrat.strategy;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/17 14:58
 * @Content
 */

@Component
public class ExchangeApiManager {
    HashMap<String, ExchangeService> exchangeMap = new HashMap<>();
    ExecutorService taskExecutor = Executors.newWorkStealingPool(2);
    ExecutorService updateOrderExecutor = Executors.newWorkStealingPool(2);
    ExecutorService orderExecutor = Executors.newWorkStealingPool(8);
    ExecutorService positionExecutor = Executors.newWorkStealingPool(8);

    public ExecutorService getTaskExecutor() {
        return taskExecutor;
    }

    public ExecutorService getOrderExecutor() {
        return orderExecutor;
    }

    public ExecutorService getUpdateOrderExecutor() {
        return updateOrderExecutor;
    }

    public ExecutorService getPositionExecutor() {
        return positionExecutor;
    }

    public ExchangeApiManager(List<ExchangeService> exchangeServices) {

        for (ExchangeService exchangeService : exchangeServices) {
            exchangeMap.put(exchangeService.getExchangeName().toLowerCase(), exchangeService);
        }
    }

    public HashMap<String, ExchangeService> getExchangeMap() {
        return exchangeMap;
    }
    public ExchangeService getExchangeService(String exchangeName) {
        return exchangeMap.get(exchangeName.toLowerCase());
    }
}
