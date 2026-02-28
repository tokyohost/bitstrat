// LeverageBracketItem.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.binance;
import java.util.List;
import lombok.Data;

@Data
public class LeverageBracketItem {
    private String symbol;
    private Double notionalCoef;
    private List<Bracket> brackets;
}
