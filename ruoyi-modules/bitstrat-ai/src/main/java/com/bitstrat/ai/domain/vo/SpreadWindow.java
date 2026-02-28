package com.bitstrat.ai.domain.vo;

import java.util.Deque;
import java.util.LinkedList;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/3 21:12
 * @Content
 */

public class SpreadWindow {
    private final Deque<SpreadRecord> window = new LinkedList<>();
    private final int minutes;

    public SpreadWindow(int minutes) {
        this.minutes = minutes;
    }

    public synchronized double[] updateAndGetRange(SpreadRecord newRecord) {
        long cutoff = newRecord.getTimestamp() - minutes * 60 * 1000L;
        window.addLast(newRecord);

        // 清理过期记录
        while (!window.isEmpty() && window.peekFirst().getTimestamp() < cutoff) {
            window.pollFirst();
        }

        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;

        for (SpreadRecord record : window) {
            double pct = record.getSpreadPct();
            if (pct > max) max = pct;
            if (pct < min) min = pct;
        }

        return new double[]{max, min};
    }
}
