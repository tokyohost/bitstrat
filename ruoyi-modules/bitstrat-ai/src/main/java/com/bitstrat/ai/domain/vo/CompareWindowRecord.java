package com.bitstrat.ai.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/3 21:13
 * @Content
 */

public class CompareWindowRecord {

    LockFreeSpreadWindow  window = new LockFreeSpreadWindow (30);

    public void handlePriceUpdate(BigDecimal priceA, BigDecimal priceB, long serverTimestamp) {
        if (priceA.compareTo(BigDecimal.ZERO) == 0 || priceB.compareTo(BigDecimal.ZERO) == 0) return;

        BigDecimal spread = (priceA.subtract(priceB).divide(priceB,10, RoundingMode.HALF_UP)).multiply(new BigDecimal(100)).setScale(4, RoundingMode.HALF_UP);
        window.add(serverTimestamp, spread.doubleValue());

    }
    public Double[] getMaxMin(long
                                  timeMin) {
        double[] maxMin = window.getMaxMin(timeMin);
        Double[] doubles = {maxMin[0], maxMin[1]};

        return doubles;
    }
}
