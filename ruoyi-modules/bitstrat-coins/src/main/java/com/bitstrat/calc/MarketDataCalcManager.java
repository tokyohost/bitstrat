package com.bitstrat.calc;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/22 18:50
 * @Content
 */

@Component
public class MarketDataCalcManager {
    HashMap<String, MarketDataCalcStragety<?>> stringMarketDataCalcStragetyHashMap = new HashMap<>();
    public MarketDataCalcManager(List<MarketDataCalcStragety<?>> strageties) {
        for (MarketDataCalcStragety<?> stragety : strageties) {
            stringMarketDataCalcStragetyHashMap.put(stragety.getType(),stragety);
        }
    }

    public MarketDataCalcStragety<?> getStragety(String type){
        for (String s : stringMarketDataCalcStragetyHashMap.keySet()) {
            if(s.equalsIgnoreCase(type)){
                return stringMarketDataCalcStragetyHashMap.get(s);
            }
        }
        return null;
    }
}
