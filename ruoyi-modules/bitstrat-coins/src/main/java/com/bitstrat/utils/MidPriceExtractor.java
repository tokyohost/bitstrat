package com.bitstrat.utils;

import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/10/31 15:56
 * @Content
 */

public class MidPriceExtractor {
    public static List<Double> getMidPrices(BarSeries series, int limit) {
        int scale = resolveScale(series);
        List<Double> mids = new ArrayList<>();
        int start = Math.max(0, series.getBarCount() - limit); // 保证不会越界
        for (int i = start; i < series.getBarCount(); i++) {
            Bar bar = series.getBar(i);
            Num mid = bar.getHighPrice()
                .plus(bar.getLowPrice())
                .dividedBy(series.numOf(2));

            mids.add(mid.bigDecimalValue().setScale(scale, RoundingMode.HALF_UP).doubleValue());
        }
        return mids;
    }
    /**
     * 计算指定周期的 EMA 序列
     *
     * @param series 数据序列
     * @param period 周期（例如 20）
     * @return EMA 指标值列表
     */
    public static List<Double> getEmaValues(BarSeries series, int period, int limit) {
        int scale = resolveScale(series);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator emaIndicator = new EMAIndicator(closePrice, period);

        List<Double> emaValues = new ArrayList<>();
        // 从 period - 1 开始计算 EMA
        for (int i = period - 1; i < series.getBarCount(); i++) {
            double v = emaIndicator.getValue(i).doubleValue();
            BigDecimal bd = BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP);
            emaValues.add(bd.doubleValue());
        }

        // 如果需要限制返回数量
        if (emaValues.size() > limit) {
            emaValues = emaValues.subList(emaValues.size() - limit, emaValues.size());
        }

        return emaValues;
    }


    /**
     * 计算指定周期的 SMA 序列
     *
     * @param series 数据序列
     * @param period 周期（例如 20）
     * @param limit  返回的最大数量
     * @return SMA 指标值列表
     */
    public static List<Double> getSmaValues(BarSeries series, int period, int limit) {
        int scale = resolveScale(series);

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator smaIndicator = new SMAIndicator(closePrice, period);

        List<Double> smaValues = new ArrayList<>();

        int barCount = series.getBarCount();
        int startIndex = period - 1; // SMA 有效起点

        for (int i = startIndex; i < barCount; i++) {
            double value = smaIndicator.getValue(i).doubleValue();
            BigDecimal bd = BigDecimal.valueOf(value)
                .setScale(scale, RoundingMode.HALF_UP);
            smaValues.add(bd.doubleValue());
        }

        // 限制返回数量（取最新 limit 条）
        if (limit > 0 && smaValues.size() > limit) {
            return new ArrayList<>(
                smaValues.subList(smaValues.size() - limit, smaValues.size())
            );
        }

        return smaValues;
    }





    public static int resolveScale(BarSeries series) {
        if (series == null || series.isEmpty()) {
            return 0;
        }
        Bar bar = series.getLastBar();
        return Math.max(
            bar.getHighPrice().bigDecimalValue().scale(),
            bar.getLowPrice().bigDecimalValue().scale()
        );
    }

    /**
     * 获取 MACD 主线值 (EMA12 - EMA26)
     *
     * @param series 数据序列
     * @param shortPeriod 短期 EMA（默认12）
     * @param longPeriod  长期 EMA（默认26）
     * @return MACD 主线值序列
     */
    public static List<Double> getMacdValues(BarSeries series, int shortPeriod, int longPeriod, int limit) {
        int scale = resolveScale(series);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(closePrice, shortPeriod, longPeriod);

        List<Double> macdValues = new ArrayList<>();
        // 从 longPeriod - 1 开始计算 MACD
        for (int i = longPeriod - 1; i < series.getBarCount(); i++) {
            double v = macd.getValue(i).doubleValue();
            BigDecimal bd = BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP);
            macdValues.add(bd.doubleValue());
        }

        // 限制返回数量，只保留最新 limit 个值
        if (macdValues.size() > limit) {
            macdValues = macdValues.subList(macdValues.size() - limit, macdValues.size());
        }

        return macdValues;
    }

    /**
     * 计算布林带指标
     *
     * @param series 数据序列
     * @param period SMA周期（默认20）
     * @param k 标准差倍数（默认2）
     * @param limit 返回的数量限制
     * @return 布林带三条线的 Map，key = "middle", "upper", "lower"
     */
    public static Map<String, List<Double>> getBollingerBands(BarSeries series, int period, double k, int limit) {
        int scale = resolveScale(series);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        SMAIndicator sma = new SMAIndicator(closePrice, period);
        StandardDeviationIndicator stdDev = new StandardDeviationIndicator(closePrice, period);

        List<Double> middle = new ArrayList<>();
        List<Double> upper = new ArrayList<>();
        List<Double> lower = new ArrayList<>();

        for (int i = period - 1; i < series.getBarCount(); i++) {
            double mb = sma.getValue(i).doubleValue();
            double sd = stdDev.getValue(i).doubleValue();

            BigDecimal mbBd = BigDecimal.valueOf(mb).setScale(scale, RoundingMode.HALF_UP);
            BigDecimal upperBd = BigDecimal.valueOf(mb + k * sd).setScale(scale, RoundingMode.HALF_UP);
            BigDecimal lowerBd = BigDecimal.valueOf(mb - k * sd).setScale(scale, RoundingMode.HALF_UP);

            middle.add(mbBd.doubleValue());
            upper.add(upperBd.doubleValue());
            lower.add(lowerBd.doubleValue());
        }

        // 限制返回数量
        if (middle.size() > limit) {
            int start = middle.size() - limit;
            middle = middle.subList(start, middle.size());
            upper = upper.subList(start, upper.size());
            lower = lower.subList(start, lower.size());
        }

        Map<String, List<Double>> result = new HashMap<>();
        result.put("middle", middle);
        result.put("upper", upper);
        result.put("lower", lower);
        return result;
    }

    /**
     * 获取 RSI 序列
     */
    public static List<Double> getRsiValues(BarSeries series, int period, int limit) {
        int scale = resolveScale(series);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, period);

        List<Double> rsiValues = new ArrayList<>();
        for (int i = period - 1; i < series.getBarCount(); i++) {
            double v = rsi.getValue(i).doubleValue();
            BigDecimal bd = BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP);
            rsiValues.add(bd.doubleValue());
        }

        // 保留最新 limit 个值
        if (rsiValues.size() > limit) {
            rsiValues = rsiValues.subList(rsiValues.size() - limit, rsiValues.size());
        }

        return rsiValues;
    }

    public static List<Double> getAtrValues(BarSeries series, int period) {
        int scale = resolveScale(series);
        ATRIndicator atr = new ATRIndicator(series, period);
        List<Double> atrValues = new ArrayList<>();
        for (int i = period - 1; i < series.getBarCount(); i++) {
            Num value = atr.getValue(i);
            BigDecimal bd = BigDecimal.valueOf(value.doubleValue()).setScale(scale, RoundingMode.HALF_UP);
            atrValues.add(bd.doubleValue());
        }
        return atrValues;
    }

    /**
     * 获取指定周期的最新 ATR 值
     *
     * @param series K 线序列
     * @param period ATR 周期
     * @return 最新 ATR 数值
     */
    public static double getCurrentAtr(BarSeries series, int period) {
        int scale = resolveScale(series);
        ATRIndicator atr = new ATRIndicator(series, period);
        // 获取最后一根 K 线的 ATR 值
        Num value = atr.getValue(series.getEndIndex());
        BigDecimal bd = BigDecimal.valueOf(value.doubleValue()).setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * 获取当前成交量
     */
    public static double getCurrentVolume(BarSeries series) {
        int scale = resolveScale(series);
        Num value = series.getBar(series.getEndIndex()).getVolume();
        BigDecimal bd = BigDecimal.valueOf(value.doubleValue()).setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * 获取平均成交量（SMA）
     *
     * @param series K线序列
     * @param period 平均周期，例如20
     */
    public static double getAverageVolume(BarSeries series, int period) {
        int scale = resolveScale(series);
        VolumeIndicator volume = new VolumeIndicator(series);
        SMAIndicator sma = new SMAIndicator(volume, period);
        Num value = sma.getValue(series.getEndIndex());
        BigDecimal bd = BigDecimal.valueOf(value.doubleValue()).setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
