package com.bitstrat.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OkxOrderSizeCalculator {

    /**
     * 根据各种参数计算可下单的合约张数（合约数量 sz）
     *
     * @param usdtAmount 想持仓的 USDT 总金额（例如 100）
     * @param leverage 杠杆倍数（例如 10）
     * @param price 当前价格（例如 BTC = 30000）
     * @param ctVal 合约面值（每张合约代表多少币）
     * @param ctMult 合约乘数（一般为 1，币本位时可能是币的单位乘数）
     * @param lotSz 下单最小单位（如 0.001）
     * @return 可下单的张数（已按精度向下取整）
     */
    public static String calculateSz(
        double usdtAmount,
        int leverage,
        double price,
        double ctVal,
        double ctMult,
        double lotSz
    ) {
        if (usdtAmount <= 0 || leverage <= 0 || price <= 0 || ctVal <= 0 || lotSz <= 0) {
            throw new IllegalArgumentException("参数不能为 0 或负数");
        }

        // 实际可用保证金（总金额 * 杠杆倍数）
        BigDecimal notional = BigDecimal.valueOf(usdtAmount).multiply(BigDecimal.valueOf(leverage));

        // 每张合约代表多少 USDT（ctVal * price * ctMult）
        BigDecimal perContractValue = BigDecimal.valueOf(ctVal)
            .multiply(BigDecimal.valueOf(price))
            .multiply(BigDecimal.valueOf(ctMult));

        // 张数 = 保证金 / 每张价值
        BigDecimal rawSz = notional.divide(perContractValue, 18, RoundingMode.DOWN);

        // 根据下单精度 round down
        int precision = countDecimalPlaces(lotSz);
        BigDecimal roundedSz = rawSz.setScale(precision, RoundingMode.DOWN);

        return roundedSz.toPlainString();
    }

    private static int countDecimalPlaces(double num) {
        BigDecimal bd = BigDecimal.valueOf(num);
        return Math.max(0, bd.stripTrailingZeros().scale());
    }

    // 示例调用
    public static void main(String[] args) {
        // 示例：100 USDT, 10倍杠杆, BTC 价格 30000, ctVal=0.01, ctMult=1, lotSz=0.001
        String sz = calculateSz(10, 1, 0.229, 10, 1, 1);
        System.out.println("应下单张数: " + sz);  // 输出例子：0.33
    }
}
