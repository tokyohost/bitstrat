package com.bitstrat.store;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Data
public class ExecuteService {

    /**
     *同步api 的余额线程池
     */
    ExecutorService apiBalanceExecute = Executors.newWorkStealingPool(5);
    /**
     * 同步返回订单处理的线程池
     */
    ExecutorService syncOrderCheck = Executors.newWorkStealingPool(2);

    /**
     * 消息通知线程池
     */
    ExecutorService notifyExecute = Executors.newWorkStealingPool(2);
    /**
     * websocket 通知前段线程池
     */
    ExecutorService websocketNotifyExecute = Executors.newWorkStealingPool(6);

    /**
     * 跑爆仓预警线程池
     */
    ExecutorService liquidationExecute = Executors.newWorkStealingPool(5);

}
