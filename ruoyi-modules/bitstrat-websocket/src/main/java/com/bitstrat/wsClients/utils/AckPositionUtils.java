package com.bitstrat.wsClients.utils;


import com.bitstrat.domain.Account;
import com.bitstrat.domain.Event.AckPositionSyncEvent;
import com.bitstrat.wsClients.domian.ConnectionConfig;
import io.netty.channel.nio.NioEventLoopGroup;
import org.dromara.common.core.utils.SpringUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/6 18:54
 * @Content
 */

public class AckPositionUtils {

    public static ScheduledFuture<?> startAckPosition(NioEventLoopGroup executors, ConnectionConfig connectionConfig, Long initSec, Long fixedSec) {
        ScheduledFuture<?> scheduledFuture = executors.scheduleWithFixedDelay(() -> {
            try {
                sendAckPosition(connectionConfig.getAccount(),connectionConfig.getExchange());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, initSec, fixedSec, TimeUnit.SECONDS);
        return scheduledFuture;
    }

    public static void sendAckPosition(Account account,String exchangeName) {
        AckPositionSyncEvent ackPositionSyncEvent = new AckPositionSyncEvent();
        ackPositionSyncEvent.setAccount(account);
        ackPositionSyncEvent.setExchangeName(exchangeName);
        SpringUtils.getApplicationContext().publishEvent(ackPositionSyncEvent);

    }
}
