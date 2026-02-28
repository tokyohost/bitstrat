// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.bybit;
import lombok.Data;

@Data
public class BybitSymbolInfo {

    /**
     * "symbol": "BTCUSDT",
     *         "contractType": "LinearPerpetual",
     *         "status": "Trading",
     *         "baseCoin": "BTC",
     *         "quoteCoin": "USDT",
     */

    String symbol;
    String baseCoin;
    String quoteCoin;
    String status;
}
