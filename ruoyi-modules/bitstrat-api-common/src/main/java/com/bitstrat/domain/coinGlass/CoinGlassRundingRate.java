// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.coinGlass;
import java.util.List;
import lombok.Data;
import java.util.Map;

@Data
public class CoinGlassRundingRate {
    private Map<String, List<Double>> frDataMap;
    private Map<String, List<Double>> dataMap;
    private List<Long> dateList;
    private List<Double> priceList;
}
