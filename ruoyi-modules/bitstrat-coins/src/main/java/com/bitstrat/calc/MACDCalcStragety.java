package com.bitstrat.calc;

import com.bitstrat.calc.constant.IndicatorType;
import com.bitstrat.domain.diy.ExtConfigItem;
import com.bitstrat.utils.MidPriceExtractor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/22 17:50
 * @Content
 */

@Service
public class MACDCalcStragety implements MarketDataCalcStragety<List<Double>> {
    @Override
    public List<Double> calc(ExtConfigItem rule, BarSeries series, int size) {
        String value = rule.getValue();
        if(StringUtils.isEmpty(value)){
            throw new RuntimeException("MACD Config ERROR,Please Check your Config");
        }
        String[] split = value.trim().split(",");
        String s1 = split[0];
        String s2 = split[1];
        if (NumberUtils.isParsable(s1) && NumberUtils.isParsable(s2)) {

            int period1 = Integer.parseInt(s1);
            int period2 = Integer.parseInt(s2);

            return MidPriceExtractor.getMacdValues(series,period1,period2,size);
        }else{
            throw new RuntimeException("MACD Config ERROR,Please Check your Config");
        }
    }

    @Override
    public String calcPrompt(ExtConfigItem rule, BarSeries series, int size) {
        List<Double> calc = this.calc(rule, series, size);
        return this.getType()+rule.getValue()+": "+calc;
    }

    @Override
    public String getType() {
        return IndicatorType.MACD.getType();
    }
}
