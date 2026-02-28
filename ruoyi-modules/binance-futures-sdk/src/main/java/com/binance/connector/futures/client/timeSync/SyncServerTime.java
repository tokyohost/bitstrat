package com.binance.connector.futures.client.timeSync;

import com.binance.connector.futures.client.enums.HttpMethod;
import com.binance.connector.futures.client.utils.RequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.util.LinkedHashMap;
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

    RequestHandler requestHandler = new RequestHandler(null, null, null);

    private synchronized void init(ScheduledExecutorService timeSync) {
        if (scheduledFuture == null) {
            log.info("SyncServerTime start");
            String baseUrl = "https://fapi.binance.com";
            String ftimeUrl = "/fapi/v1/time";
            this.timeSync = timeSync;
            RequestHandler.finalServerTimeSource.set(new ServerTimeSource(System.currentTimeMillis()));
            //每15秒同步一次时间
            this.scheduledFuture = this.timeSync.scheduleWithFixedDelay(() -> {
                try {
                    String s = requestHandler.sendPublicRequest(baseUrl, ftimeUrl, new LinkedHashMap<>(), HttpMethod.GET, false);
                    JSONObject jsonObject = new JSONObject(s);
                    long aLong = jsonObject.getLong("serverTime");
                    RequestHandler.finalServerTimeSource.set(new ServerTimeSource(aLong));
                    long currented = System.currentTimeMillis();
                    log.info("币安 同步 serverTime ：{}  当前系统时间：{} 相差时间：{}", aLong, currented, aLong - currented);

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
