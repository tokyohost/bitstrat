package com.bitstrat.ai.domain.vo;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/3 21:11
 * @Content
 */

public class SpreadRecord {
    private final long timestamp;  // 毫秒时间戳
    private final double spreadPct;

    public SpreadRecord(long timestamp, double spreadPct) {
        this.timestamp = timestamp;
        this.spreadPct = spreadPct;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getSpreadPct() {
        return spreadPct;
    }
}
