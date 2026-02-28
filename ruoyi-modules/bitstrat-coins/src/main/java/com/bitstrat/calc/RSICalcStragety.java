package com.bitstrat.calc;

import com.bitstrat.calc.constant.IndicatorType;
import com.bitstrat.domain.diy.ExtConfigItem;
import com.bitstrat.utils.MidPriceExtractor;
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
public class RSICalcStragety implements MarketDataCalcStragety<List<Double>> {
    @Override
    public List<Double> calc(ExtConfigItem rule, BarSeries series, int size) {
        String value = rule.getValue();
        if (NumberUtils.isParsable(value)) {
            int period = Integer.parseInt(value);

            return MidPriceExtractor.getRsiValues(series,period,size);
        }else{
            throw new RuntimeException("RSI Config ERROR,Please Check your Config");
        }
    }

    @Override
    public String calcPrompt(ExtConfigItem rule, BarSeries series, int size) {
        List<Double> calc = this.calc(rule, series, size);
        return this.getType()+rule.getValue()+": "+calc;
    }

    @Override
    public String getType() {
        return IndicatorType.RSI.getType();
    }
}
