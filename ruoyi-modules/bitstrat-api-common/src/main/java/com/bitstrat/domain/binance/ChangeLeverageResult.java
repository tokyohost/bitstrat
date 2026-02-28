// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.binance;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * {
 *  	"leverage": 21,	// 杠杆倍数
 *  	"maxNotionalValue": "1000000", // 当前杠杆倍数下允许的最大名义价值
 *  	"symbol": "BTCUSDT"	// 交易对
 * }
 */
@Data
public class ChangeLeverageResult {
    private Integer leverage;
    private String symbol;
    private BigDecimal maxNotionalValue;
}
