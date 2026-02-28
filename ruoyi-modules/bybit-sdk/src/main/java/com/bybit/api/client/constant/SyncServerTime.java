package com.bybit.api.client.constant;

import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/12 11:38
 * @Content
 */

@Slf4j
public class SyncServerTime {
    static ScheduledExecutorService timeSync;
    static ScheduledFuture<?> scheduledFuture = null;
    public SyncServerTime(ScheduledExecutorService timeSync) {
        init(timeSync);
    }


    private synchronized void init(ScheduledExecutorService timeSync) {
        if (scheduledFuture == null) {
            log.info("SyncServerTime start");
            this.timeSync = timeSync;
            Helper.finalServerTimeSource.set(new ServerTimeSource(System.currentTimeMillis()));
            //每15秒同步一次时间
            this.scheduledFuture = this.timeSync.scheduleWithFixedDelay(() -> {
                try {
                    BybitApiMarketRestClient bybitApiMarketRestClient = BybitApiClientFactory.newInstance().newMarketDataRestClient();
                    Object serverTime = bybitApiMarketRestClient.getServerTime();
                    LinkedHashMap<String,Object> resp = (LinkedHashMap<String,Object>) serverTime;

                    if ((Integer) resp.get("retCode") == 0) {
                        long aLong = (Long) resp.get("time");
                        Helper.finalServerTimeSource.set(new ServerTimeSource(aLong));
                        long currented = System.currentTimeMillis();
                        log.info("bybit 同步 serverTime ：{}  当前系统时间：{} 相差时间：{}", aLong, currented, aLong - currented);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 5, 15, TimeUnit.SECONDS);
        }

    }

    public void sync() {
        log.info("no sync time");
    }
}
