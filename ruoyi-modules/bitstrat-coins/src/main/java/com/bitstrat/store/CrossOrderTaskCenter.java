package com.bitstrat.store;

import com.bitstrat.utils.BitStratThreadFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class CrossOrderTaskCenter {
    private final static ConcurrentHashMap<String, ScheduledFuture<?>> queryOrderScheduledMap = new ConcurrentHashMap<>();
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, BitStratThreadFactory.forName("queryOrder-Schedule-scheduler"));

    public static ConcurrentHashMap<String, ScheduledFuture<?>> getQueryOrderScheduledMap() {
        return queryOrderScheduledMap;
    }

    public static ScheduledExecutorService getScheduler() {
        return scheduler;
    }
}
