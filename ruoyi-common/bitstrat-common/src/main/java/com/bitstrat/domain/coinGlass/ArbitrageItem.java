// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.coinGlass;
import java.util.List;
import lombok.Data;

@Data
public class ArbitrageItem {
    private List<Margin> cMarginList;
    private List<Margin> uMarginList;
    private String symbol;
    private Double uIndexPrice;
    private Double uPrice;
    private Double cPrice;
    private String symbolLogo;
    private Double cIndexPrice;
    private Long status;
}
