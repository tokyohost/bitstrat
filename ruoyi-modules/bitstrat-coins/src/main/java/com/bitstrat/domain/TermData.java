package com.bitstrat.domain;

import com.bitstrat.domain.vo.SymbolFundingRate;
import lombok.Data;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/10/31 16:04
 * @Content
 */

@Data
public class TermData {

    ClosePriceIndicator closePrice;

    EMAIndicator ema20;
    EMAIndicator ema50;

    MACDIndicator macd;

    EMAIndicator macdSignal;

    RSIIndicator rsi7;
    RSIIndicator rsi14;

    BigDecimal openInterest;

    SymbolFundingRate symbolFundingRate;

    List<Double> midPrices;

    List<Double> ema20periods;
    List<Double> ema50periods;

    List<Double> macdIndicators;

    List<Double> rsi7period;

    Map<String, List<Double>> bollingerBands;

    List<Double> rsi14period;


    BigDecimal atr3;
    BigDecimal atr14;

    BigDecimal currentVolume;
    BigDecimal averageVolume;

    public CharSequence getEma20Value() {
        Num value = ema20.getValue(ema20.getBarCount() - 1);
        return value.bigDecimalValue().stripTrailingZeros().toPlainString();
    }
    public CharSequence getEma50Value() {
        Num value = ema50.getValue(ema50.getBarCount() - 1);
        return value.bigDecimalValue().stripTrailingZeros().toPlainString();
    }

    public CharSequence getMacdValue() {
        Num value = macd.getValue(macd.getBarSeries().getEndIndex());
        return value.bigDecimalValue().stripTrailingZeros().toPlainString();
    }

    public CharSequence getRsi7Value() {
        return rsi7.getValue(rsi7.getBarSeries().getEndIndex()).bigDecimalValue().stripTrailingZeros().toPlainString();
    }
    public CharSequence getRsi14Value() {
        return rsi14.getValue(rsi14.getBarSeries().getEndIndex()).bigDecimalValue().stripTrailingZeros().toPlainString();
    }

    public String getBollingerBandsValue() {
        //输出为 "middle", "upper", "lower"
        /**
         * Bollinger Bands (20-period, 2.0 ×StdDev):
         * middle: [1,1,1,1....]
         * upper: [1,1,1,1....]
         * lower: [1,1,1,1....]
         */

        StringBuilder sb = new StringBuilder();
        sb.append("Bollinger Bands (20-period, 2.0×StdDev):\n");
        bollingerBands.forEach((k, v) -> sb.append(k).append(": ").append(v.stream()
            .map(d -> BigDecimal.valueOf(d).toPlainString())
            .collect(Collectors.toList())).append("\n"));
        return sb.toString();
    }

    public List<String> getMacdIndicatorsString() {
        return macdIndicators.stream()
            .map(d -> BigDecimal.valueOf(d).toPlainString())
            .collect(Collectors.toList());
    }
    public List<String> getEma20PeriodString() {
        return ema20periods.stream()
            .map(d -> BigDecimal.valueOf(d).toPlainString())
            .collect(Collectors.toList());
    }
    public List<String> getMidPriceString() {
        return midPrices.stream()
            .map(d -> BigDecimal.valueOf(d).toPlainString())
            .collect(Collectors.toList());
    }
    public List<String> getrsi7periodString() {
        return rsi7period.stream()
            .map(d -> BigDecimal.valueOf(d).toPlainString())
            .collect(Collectors.toList());
    }
    public List<String> getrsi14periodString() {
        return rsi14period.stream()
            .map(d -> BigDecimal.valueOf(d).toPlainString())
            .collect(Collectors.toList());
    }

}
