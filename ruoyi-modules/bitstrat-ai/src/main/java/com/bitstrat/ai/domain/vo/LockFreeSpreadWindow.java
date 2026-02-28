package com.bitstrat.ai.domain.vo;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/3 21:15
 * @Content
 */

public class LockFreeSpreadWindow {
    private static final int CAPACITY = 10000;  // 可覆盖最大时间窗口数据（例如 30 分钟 * 每秒采样）

    private final long[] timestamps = new long[CAPACITY];
    private final Double[] spreads = new Double[CAPACITY];
    private final AtomicInteger index = new AtomicInteger(0); // 当前写入位置（循环使用）
    private final int windowMinutes;

    public LockFreeSpreadWindow(int windowMinutes) {
        this.windowMinutes = windowMinutes;
    }

    public void add(long timestamp, double spreadPct) {
        int i = index.getAndIncrement() % CAPACITY;
        timestamps[i] = timestamp;
        spreads[i] = spreadPct;
    }

    public double[] getMaxMin(long now) {
        long cutoff = now - windowMinutes * 60_000L;
        double max = -999;
        double min = 999;

        for (int i = 0; i < CAPACITY; i++) {
            long ts = timestamps[i];
            if (ts >= cutoff) {
                Double v = spreads[i];
                if(v == null) continue; // 跳过未设置的值
                if (v > max) max = v;
                if (v < min) min = v;
            }
        }

        return new double[]{max, min};
    }
}
