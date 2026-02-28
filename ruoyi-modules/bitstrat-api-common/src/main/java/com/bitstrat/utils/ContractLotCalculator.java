package com.bitstrat.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ContractLotCalculator {

    public static class OrderResult {
        private final BigDecimal lots;
        private final boolean isValid;

        public OrderResult(BigDecimal lots, boolean isValid) {
            this.lots = lots;
            this.isValid = isValid;
        }

        public BigDecimal getLots() {
            return lots;
        }

        public boolean isValid() {
            return isValid;
        }

        @Override
        public String toString() {
            return "张数: " + lots.toPlainString() + ", 是否有效: " + isValid;
        }
    }

    /**
     * 计算下单 BTC 数量对应的合约张数
     *
     * @param size      用户想下单的 BTC 数量，例如 0.0001
     * @param faceValue   每张合约的面值，例如 0.01
     * @param minOrderLots   最小下单张数，例如 0.01
     * @return OrderResult   返回张数与是否满足最小下单限制
     */
    public static OrderResult calculateLots(BigDecimal size, BigDecimal faceValue, BigDecimal minOrderLots) {
        if (faceValue.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("合约面值不能为 0");
        }

        // lots = amount / faceValue
        BigDecimal lots = size.divide(faceValue, 10, RoundingMode.DOWN);

        // 根据 minOrderLots 的精度保留小数位
        int scale = minOrderLots.scale();
        BigDecimal roundedLots = lots.setScale(scale, RoundingMode.DOWN);

        boolean isValid = roundedLots.compareTo(minOrderLots) >= 0;

        return new OrderResult(roundedLots, isValid);
    }

    // 示例调用
    public static void main(String[] args) {
        BigDecimal amountBTC = new BigDecimal("500");
        BigDecimal faceValueBTC = new BigDecimal("1000");
        BigDecimal minOrderLots = new BigDecimal("0.01");

        OrderResult result = calculateLots(amountBTC, faceValueBTC, minOrderLots);
        System.out.println(result); // 输出: 张数: 0.01, 是否有效: true
    }
}
