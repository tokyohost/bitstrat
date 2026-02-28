package com.bitstrat.ai.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceDiffUtil {
    public static BigDecimal calcLongAShortB(BigDecimal priceA, BigDecimal priceB) {
        return priceB.subtract(priceA)
            .divide(priceA, 8, RoundingMode.DOWN)
            .multiply(BigDecimal.valueOf(100))
            .setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcShortALongB(BigDecimal priceA, BigDecimal priceB) {
        return priceA.subtract(priceB)
            .divide(priceB, 8, RoundingMode.DOWN)
            .multiply(BigDecimal.valueOf(100))
            .setScale(4, RoundingMode.HALF_UP);
    }
}
