package com.bitstrat.strategy;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/2 11:27
 * @Content
 */

@Component
public class StrategyManager {
    HashMap<Integer, NormalStrategy> strategyMap = new HashMap<>();
    List<NormalStrategy> strategyList = new ArrayList<>();
    public StrategyManager(List<NormalStrategy> strategyList) {
        for (NormalStrategy normalStrategy : strategyList) {
            strategyMap.put(normalStrategy.typeId(), normalStrategy);
        }
        this.strategyList = strategyList;
    }

    public NormalStrategy getStrategy(int typeId) {
        return strategyMap.get(typeId);
    }

    public List<NormalStrategy> getStrategyList() {
        return strategyList;
    }
}
