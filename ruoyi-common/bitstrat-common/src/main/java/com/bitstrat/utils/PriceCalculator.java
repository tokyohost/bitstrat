package com.bitstrat.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceCalculator {

    public static BigDecimal calculateMidPrice(BigDecimal price1, BigDecimal price2) {
        // 获取价格的小数位数
        int scale1 = price1.stripTrailingZeros().scale();
        int scale2 = price2.stripTrailingZeros().scale();

        // 使用较大的小数位数进行计算
        int maxScale = Math.max(scale1, scale2);

        // 计算中间价
        BigDecimal midPrice = price1.add(price2).divide(BigDecimal.valueOf(2), maxScale, RoundingMode.HALF_UP);

        return midPrice;
    }

}
