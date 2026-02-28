package com.bitstrat.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/7/22 16:01
 * @Content
 */


public class ArbitrageProfitCalculator {

    /**
     * 计算平仓后的净盈亏（正数为盈利，负数为亏损）
     *
     * @param aOpen         A交易所开仓价
     * @param bOpen         B交易所开仓价
     * @param aNow          A当前价
     * @param bNow          B当前价
     * @param aDir          A方向：1=做多，-1=做空
     * @param bDir          B方向：1=做多，-1=做空
     * @param fundingIncome 资金费收益（单位：USDT）
     * @param aFeeRate      A交易所手续费率（开+平）
     * @param bFeeRate      B交易所手续费率
     * @param aAmount       A仓位数量
     * @param bAmount       B仓位数量
     * @return 净盈亏金额（BigDecimal，单位：USDT）
     */
    public static BigDecimal calcNetProfit(
        BigDecimal aOpen, BigDecimal bOpen,
        BigDecimal aNow, BigDecimal bNow,
        int aDir, int bDir,
        BigDecimal fundingIncome,
        BigDecimal aFeeRate, BigDecimal bFeeRate,
        BigDecimal aAmount, BigDecimal bAmount
    ) {
        BigDecimal aProfit = aNow.subtract(aOpen)
            .multiply(BigDecimal.valueOf(aDir))
            .multiply(aAmount);

        BigDecimal bProfit = bNow.subtract(bOpen)
            .multiply(BigDecimal.valueOf(bDir))
            .multiply(bAmount);

        BigDecimal aFee = aOpen.add(aNow).multiply(aFeeRate).multiply(aAmount);
        BigDecimal bFee = bOpen.add(bNow).multiply(bFeeRate).multiply(bAmount);

        return aProfit.add(bProfit).add(fundingIncome).subtract(aFee).subtract(bFee)
            .setScale(8, RoundingMode.HALF_UP);
    }

    public static void main(String[] args) {
        BigDecimal aOpen = new BigDecimal("100.00000001");
        BigDecimal bOpen = new BigDecimal("99.20000012");
        BigDecimal aNow = new BigDecimal("120.00000005");
        BigDecimal bNow = new BigDecimal("120.00000005");

        int aDir = 1;  // A做多
        int bDir = -1; // B做空

        BigDecimal fundingIncome = new BigDecimal("0.0005"); // 资金费

        BigDecimal aFeeRate = new BigDecimal("0.0002");
        BigDecimal bFeeRate = new BigDecimal("0.0002");

        BigDecimal aAmount = new BigDecimal("1.0");
        BigDecimal bAmount = new BigDecimal("1.0");

        BigDecimal netProfit = calcNetProfit(
            aOpen, bOpen, aNow, bNow,
            aDir, bDir,
            fundingIncome,
            aFeeRate, bFeeRate,
            aAmount, bAmount
        );

        System.out.println("净盈亏：" + netProfit.stripTrailingZeros().toPlainString() + " USDT");

        if (netProfit.compareTo(BigDecimal.ZERO) >= 0) {
            System.out.println("✅ 可平仓（不亏）");
        } else {
            System.out.println("❌ 不可平仓（亏损）");
        }
    }
}
