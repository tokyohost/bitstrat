package com.bitstrat.store.orderCheck;


import com.bitstrat.domain.Account;
import com.bitstrat.domain.CoinsOrder;
import com.bitstrat.domain.bo.CoinsOrderBo;
import com.bitstrat.domain.vo.CoinsOrderVo;
import com.bitstrat.domain.vo.CoinsOrderVoToCoinsOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.MapstructUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.*;
/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/25 19:01
 * @Content
 */

@Slf4j
@Component
public class OrderCheckerManager {

    private static ScheduledExecutorService executor;

    @PostConstruct
    public void init() {

        executor = Executors.newScheduledThreadPool(4);
        log.info("OrderCheckerManager 初始化");
    }

    @PreDestroy
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
            log.info("OrderCheckerManager 已关闭");
        }
    }

    /**
     * 启动检查任务（首次延迟执行）
     */
    public void submit(CoinsOrderVo order, Account account, int maxRetry, long delayMillis) {
        CoinsOrder convert = MapstructUtils.convert(order, CoinsOrder.class);

        OrderUpdateTask task = new OrderUpdateTask(convert, account, maxRetry, delayMillis);
        executor.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 启动检查任务（首次延迟执行）
     */
    public void submit(CoinsOrderBo order, Account account, int maxRetry, long delayMillis) {
        CoinsOrder convert = MapstructUtils.convert(order, CoinsOrder.class);
        OrderUpdateTask task = new OrderUpdateTask(convert, account, maxRetry, delayMillis);
        executor.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 提供给 Task 使用的静态重调度方法
     */
    public static void scheduleLater(Runnable task, long delayMillis) {
        executor.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
    }
}
