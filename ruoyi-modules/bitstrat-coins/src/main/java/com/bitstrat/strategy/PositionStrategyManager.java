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
public class PositionStrategyManager {
    HashMap<Integer, PositionStrategy> strategyMap = new HashMap<>();
    List<PositionStrategy> strategyList = new ArrayList<>();
    public PositionStrategyManager(List<PositionStrategy> strategyList) {
        for (PositionStrategy strategy : strategyList) {
            strategyMap.put(strategy.typeId(), strategy);
        }
        this.strategyList = strategyList;
    }

    public PositionStrategy getStrategy(int typeId) {
        return strategyMap.get(typeId);
    }

    public List<PositionStrategy> getStrategyList() {
        return strategyList;
    }
}
