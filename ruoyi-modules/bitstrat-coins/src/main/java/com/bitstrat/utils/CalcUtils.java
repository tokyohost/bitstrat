package com.bitstrat.utils;

import com.bitstrat.domain.Account;
import com.bitstrat.domain.TermData;
import com.bitstrat.domain.diy.ExtConfigItem;
import com.bitstrat.domain.diy.MarketDataPromptRule;
import com.bitstrat.domain.vo.SymbolFundingRate;
import com.bitstrat.strategy.ExchangeService;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/10/31 16:08
 * @Content
 */

public class CalcUtils {

    public static TermData calcShortTerm(BarSeries series,int limitDataSize) {
        // 2️⃣ 获取收盘价指标
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        // 3️⃣ 计算 EMA(20)
        EMAIndicator ema20 = new EMAIndicator(closePrice, 20);

        // 4️⃣ 计算 MACD(12, 26)
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);

        // c（Signal线）通常是 MACD 的9期EMA
        EMAIndicator macdSignal = new EMAIndicator(macd, 9);

        // 5️⃣ 计算 RSI(7)
        RSIIndicator rsi7 = new RSIIndicator(closePrice, 7);
        RSIIndicator rsi14 = new RSIIndicator(closePrice, 14);


        //获取mid price
        List<Double> midPrices = MidPriceExtractor.getMidPrices(series,limitDataSize);
        //EMA indicators (20‑period)
        List<Double> ema20periods = MidPriceExtractor.getEmaValues(series,20,limitDataSize);
        //MACD indicators
        List<Double> macdIndicators = MidPriceExtractor.getMacdValues(series,12,26,limitDataSize);
        //RSI indicators (7‑Period)
        List<Double> rsi7period = MidPriceExtractor.getRsiValues(series,7,limitDataSize);
        List<Double> rsi14period = MidPriceExtractor.getRsiValues(series,14,limitDataSize);

        //Bollinger Bands (20-period, 2×StdDev)
        Map<String, List<Double>> bollingerBands = MidPriceExtractor.getBollingerBands(series, 20, 2, limitDataSize);

        TermData termData = new TermData();
        termData.setClosePrice(closePrice);
        termData.setEma20(ema20);
        termData.setMacd(macd);
        termData.setMacdSignal(macdSignal);
        termData.setRsi7(rsi7);
        termData.setRsi14(rsi14);
        termData.setMidPrices(midPrices);
        termData.setEma20periods(ema20periods);
        termData.setMacdIndicators(macdIndicators);
        termData.setRsi14period(rsi14period);
        termData.setRsi7period(rsi7period);
        termData.setBollingerBands(bollingerBands);


        return termData;
    }

    public static TermData calcLongerTerm(BarSeries series,int limitDataSize) {
// 2️⃣ 获取收盘价指标
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        // 3️⃣ 计算 EMA(20)
        EMAIndicator ema20 = new EMAIndicator(closePrice, 20);

        EMAIndicator ema50 = new EMAIndicator(closePrice, 50);

        // 4️⃣ 计算 MACD(12, 26)
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);

        // DEA（Signal线）通常是 MACD 的9期EMA
        EMAIndicator macdSignal = new EMAIndicator(macd, 9);

        // 5️⃣ 计算 RSI(7)
        RSIIndicator rsi7 = new RSIIndicator(closePrice, 7);
        RSIIndicator rsi14 = new RSIIndicator(closePrice, 14);

        //获取mid price
        List<Double> midPrices = MidPriceExtractor.getMidPrices(series,limitDataSize);
        //EMA indicators (20‑period)
        List<Double> ema20periods = MidPriceExtractor.getEmaValues(series,20,limitDataSize);
        List<Double> ema50periods = MidPriceExtractor.getEmaValues(series,50,limitDataSize);
        //MACD indicators
        List<Double> macdIndicators = MidPriceExtractor.getMacdValues(series,12,26,limitDataSize);
        //RSI indicators (7‑Period)
        List<Double> rsi7period = MidPriceExtractor.getRsiValues(series,7,limitDataSize);
        List<Double> rsi14period = MidPriceExtractor.getRsiValues(series,14,limitDataSize);

        double atr3 = MidPriceExtractor.getCurrentAtr(series, 3);
        double atr14 = MidPriceExtractor.getCurrentAtr(series, 14);

        double currentVolume = MidPriceExtractor.getCurrentVolume(series);
        double averageVolume = MidPriceExtractor.getAverageVolume(series, series.getBarCount());

        //Bollinger Bands (20-period, 2×StdDev)
        Map<String, List<Double>> bollingerBands = MidPriceExtractor.getBollingerBands(series, 20, 2, limitDataSize);

        TermData termData = new TermData();
        termData.setClosePrice(closePrice);
        termData.setEma20(ema20);
        termData.setEma50(ema50);
        termData.setMacd(macd);
        termData.setMacdSignal(macdSignal);
        termData.setRsi7(rsi7);
        termData.setRsi14(rsi14);
        termData.setMidPrices(midPrices);
        termData.setEma20periods(ema20periods);
        termData.setEma50periods(ema50periods);
        termData.setMacdIndicators(macdIndicators);
        termData.setRsi14period(rsi14period);
        termData.setRsi7period(rsi7period);
        termData.setBollingerBands(bollingerBands);

        termData.setAtr3(BigDecimal.valueOf(atr3));
        termData.setAtr14(BigDecimal.valueOf(atr14));
        termData.setCurrentVolume(BigDecimal.valueOf(currentVolume));
        termData.setAverageVolume(BigDecimal.valueOf(averageVolume));
        return termData;
    }
    public static TermData calcMiddleTerm(BarSeries series,int limitDataSize) {
// 2️⃣ 获取收盘价指标
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        // 3️⃣ 计算 EMA(20)
        EMAIndicator ema20 = new EMAIndicator(closePrice, 20);

        EMAIndicator ema50 = new EMAIndicator(closePrice, 50);

        // 4️⃣ 计算 MACD(12, 26)
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);

        // DEA（Signal线）通常是 MACD 的9期EMA
        EMAIndicator macdSignal = new EMAIndicator(macd, 9);

        // 5️⃣ 计算 RSI(7)
        RSIIndicator rsi7 = new RSIIndicator(closePrice, 7);
        RSIIndicator rsi14 = new RSIIndicator(closePrice, 14);

        //获取mid price
        List<Double> midPrices = MidPriceExtractor.getMidPrices(series,limitDataSize);
        //EMA indicators (20‑period)
        List<Double> ema20periods = MidPriceExtractor.getEmaValues(series,20,limitDataSize);
        List<Double> ema50periods = MidPriceExtractor.getEmaValues(series,50,limitDataSize);
        //MACD indicators
        List<Double> macdIndicators = MidPriceExtractor.getMacdValues(series,12,26,limitDataSize);
        //RSI indicators (7‑Period)
        List<Double> rsi7period = MidPriceExtractor.getRsiValues(series,7,limitDataSize);
        List<Double> rsi14period = MidPriceExtractor.getRsiValues(series,14,limitDataSize);

        double atr3 = MidPriceExtractor.getCurrentAtr(series, 3);
        double atr14 = MidPriceExtractor.getCurrentAtr(series, 14);

        double currentVolume = MidPriceExtractor.getCurrentVolume(series);
        double averageVolume = MidPriceExtractor.getAverageVolume(series, series.getBarCount());

        //Bollinger Bands (20-period, 2×StdDev)
        Map<String, List<Double>> bollingerBands = MidPriceExtractor.getBollingerBands(series, 20, 2, limitDataSize);

        TermData termData = new TermData();
        termData.setClosePrice(closePrice);
        termData.setEma20(ema20);
        termData.setEma50(ema50);
        termData.setMacd(macd);
        termData.setMacdSignal(macdSignal);
        termData.setRsi7(rsi7);
        termData.setRsi14(rsi14);
        termData.setMidPrices(midPrices);
        termData.setEma20periods(ema20periods);
        termData.setEma50periods(ema50periods);
        termData.setMacdIndicators(macdIndicators);
        termData.setRsi14period(rsi14period);
        termData.setRsi7period(rsi7period);
        termData.setBollingerBands(bollingerBands);

        termData.setAtr3(BigDecimal.valueOf(atr3));
        termData.setAtr14(BigDecimal.valueOf(atr14));
        termData.setCurrentVolume(BigDecimal.valueOf(currentVolume));
        termData.setAverageVolume(BigDecimal.valueOf(averageVolume));
        return termData;
    }

    public static boolean checkHasDataByName(MarketDataPromptRule rule, String name,Boolean isDefault) {
        List<ExtConfigItem> extConfigItems = rule.getExtConfigItems();
        for (ExtConfigItem extConfigItem : extConfigItems) {
            if (name.equalsIgnoreCase(extConfigItem.getType()) && isDefault.equals(extConfigItem.getIsDefault())) {
                return true;
            }
        }
        return false;
    }
}
