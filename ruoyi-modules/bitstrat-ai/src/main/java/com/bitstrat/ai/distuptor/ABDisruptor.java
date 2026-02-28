package com.bitstrat.ai.distuptor;

import com.bitstrat.ai.domain.abOrder.ABOrderTask;
import com.bitstrat.ai.handler.MarketABOrderStore;
import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/4 17:35
 * @Content 监听两个交易所对应的实时价格
 */
@Data
public class ABDisruptor {
    MarketPriceDisruptor disruptorA;
    MarketPriceDisruptor disruptorB;
    private final ReentrantLock lock = new ReentrantLock();
    String taskId;
    Long userId;
    ABOrderTask abOrderTask;
    ConcurrentHashMap<String, MarketPriceDisruptor> disruptorMap = new ConcurrentHashMap<>();

    public ABDisruptor(MarketPriceDisruptor disruptorA, MarketPriceDisruptor disruptorB, ABOrderTask abOrderTask
    ) {
        this.disruptorA = disruptorA;
        this.disruptorB = disruptorB;
        this.taskId = abOrderTask.getTaskId();
        this.userId = abOrderTask.getUserId();
        this.abOrderTask = abOrderTask;
        disruptorMap.put(getDisruptorKey(disruptorA.getExchangeName(), disruptorA.getSymbol()), disruptorA);
        disruptorMap.put(getDisruptorKey(disruptorB.getExchangeName(), disruptorB.getSymbol()), disruptorB);

    }

    private String getDisruptorKey(String exchangeName,String symbol) {
        return symbol + ":" + exchangeName;
    }

    public MarketPriceDisruptor getDisruptor(String exchangeName, String symbol) {
        return disruptorMap.getOrDefault(getDisruptorKey(exchangeName, symbol),null);
    }

    /**
     * 停止监听，并关闭 disruptor
     */
    public void close() {
        MarketABOrderStore.CloseABDisruptor(this.abOrderTask);
    }

    public void shutdown() {
        this.disruptorA.shutdown();
        this.disruptorB.shutdown();
    }
}
