package com.bybit.api.client.constant;

import com.bybit.api.client.restApi.BybitApiAccountRestClient;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import lombok.extern.slf4j.Slf4j;
import net.time4j.SystemClock;
import net.time4j.ZonalClock;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class
 */
@Slf4j
public final class Helper {
    final static ScheduledExecutorService timeSync = Executors.newScheduledThreadPool(1);
    static AtomicReference<ServerTimeSource> finalServerTimeSource = new AtomicReference<>(null);
    static SyncServerTime syncServerTime ;

    public static long generateTimestamp() {
        ServerTimeSource serverTimeSource = finalServerTimeSource.get();
        if (serverTimeSource != null) {
            return serverTimeSource.currentTime().toTemporalAccessor().toEpochMilli();
        }else{
            synchronized (Helper.class) {
                if(syncServerTime == null) {
                    syncServerTime = new SyncServerTime(timeSync);
                }
            }
        }
        return Instant.now().toEpochMilli();
    }

    private Helper() {

    }

    public static Map<String, Object> convertQueryToMap(String query) {
        Map<String, Object> result = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return result;
        }

        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            }
        }

        return result;
    }

    public static String generateTransferID()
    {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static String listToString(List<String> items) {
        return String.join(",", items);
    }
}
