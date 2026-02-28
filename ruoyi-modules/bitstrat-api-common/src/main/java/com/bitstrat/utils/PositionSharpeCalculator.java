package com.bitstrat.utils;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/11/2 22:23
 * @Content
 */

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sharpe Ratio Calculator for historical closed positions
 */
public class PositionSharpeCalculator {

    // Position 数据结构
    public static class Position {
        String symbol;
        double pnlRate;   // 每笔仓位收益率（如 0.05 表示 +5%，-0.03 表示 -3%）
        double duration;  // 持仓时长（小时或天，用于加权可选）

        public Position(String symbol, double pnlRate, double duration) {
            this.symbol = symbol;
            this.pnlRate = pnlRate;
            this.duration = duration;
        }
    }

    /**
     * 计算 Sharpe Ratio （简单版本，未加权）
     * @param returns 每笔仓位的收益率
     * @param riskFreeRate 无风险利率（可设为0）
     * @return Sharpe Ratio
     */
    public static double calculateSharpeRatio(List<Double> returns, double riskFreeRate) {
        if (returns == null || returns.size() < 2) return 0.0;

        // 平均收益
        double avg = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        // 方差 & 标准差
        double variance = returns.stream()
            .mapToDouble(r -> Math.pow(r - avg, 2))
            .average().orElse(0.0);
        double stdDev = Math.sqrt(variance);

        return stdDev == 0 ? 0.0 : (avg - riskFreeRate) / stdDev;
    }

    /**
     * 计算加权 Sharpe Ratio（考虑持仓时间加权）
     */
    public static double calculateWeightedSharpeRatio(List<Position> positions, double riskFreeRate) {
        if (positions == null || positions.size() < 2) return 0.0;

        double totalDuration = positions.stream().mapToDouble(p -> p.duration).sum();

        // 加权平均收益
        double weightedAvg = positions.stream()
            .mapToDouble(p -> p.pnlRate * (p.duration / totalDuration))
            .sum();

        // 加权方差
        double variance = positions.stream()
            .mapToDouble(p -> Math.pow(p.pnlRate - weightedAvg, 2) * (p.duration / totalDuration))
            .sum();

        double stdDev = Math.sqrt(variance);
        return stdDev == 0 ? 0.0 : (weightedAvg - riskFreeRate) / stdDev;
    }

    public static void main(String[] args) {
        // 示例历史仓位数据
        List<Position> history = Arrays.asList(
            new Position("BTCUSDT", 0.08, 5.0),
            new Position("ETHUSDT", -0.03, 3.0),
            new Position("SOLUSDT", 0.10, 8.0),
            new Position("XRPUSDT", 0.02, 2.0),
            new Position("LINKUSDT", -0.01, 1.5)
        );

        // 提取收益率列表
        List<Double> returns = history.stream().map(p -> p.pnlRate).collect(Collectors.toList());

        // 假设无风险利率为 0
        double riskFreeRate = 0.0;

        double sharpe = calculateSharpeRatio(returns, riskFreeRate);
        double weightedSharpe = calculateWeightedSharpeRatio(history, riskFreeRate);

        System.out.printf("Simple Sharpe Ratio: %.4f%n", sharpe);
        System.out.printf("Weighted Sharpe Ratio: %.4f%n", weightedSharpe);
    }
}
