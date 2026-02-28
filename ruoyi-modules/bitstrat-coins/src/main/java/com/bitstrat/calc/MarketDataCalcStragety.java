package com.bitstrat.calc;

import com.bitstrat.domain.MarketData;
import com.bitstrat.domain.diy.ExtConfigItem;
import org.apache.poi.ss.formula.functions.T;
import org.ta4j.core.BarSeries;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/22 17:43
 * @Content
 */
public interface MarketDataCalcStragety<T>{

    /**
     * 根据不同规则计算出不同的指标
     * @param rule 计算规则
     * @param series K线情况
     * @param size 长度 如果有的话
     * @return
     */
    T calc(ExtConfigItem rule, BarSeries series, int size);

    String calcPrompt(ExtConfigItem rule, BarSeries series, int size);


    String getType();
}
