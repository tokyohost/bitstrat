package com.bitstrat.init;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SyncStatusContext {
    private static  final AtomicInteger atomicInteger = new AtomicInteger(0);
    private static  final AtomicReference<LocalDateTime> lastFinishTime = new AtomicReference<>(LocalDateTime.now());
    public static int getSyncStatus() {
        return  atomicInteger.get();
    }
    public static String getLastFinishTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return  formatter.format(lastFinishTime.get());
    }
    public static void start() {
        atomicInteger.set(1);
    }
    public static void stop() {
        atomicInteger.set(0);
        lastFinishTime.set(LocalDateTime.now());
    }
}
