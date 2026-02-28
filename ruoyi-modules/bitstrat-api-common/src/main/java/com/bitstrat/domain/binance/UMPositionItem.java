// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.binance;
import java.math.BigDecimal;

import lombok.Data;

/**
 * {
 *   		"entryPrice": "0.00000", // 开仓均价
 * 		"breakEvenPrice": "0.0",  // 盈亏平衡价
 *   		"marginType": "isolated", // 逐仓模式或全仓模式
 *   		"isAutoAddMargin": "false",
 *   		"isolatedMargin": "0.00000000",	// 逐仓保证金
 *   		"leverage": "10", // 当前杠杆倍数
 *   		"liquidationPrice": "0", // 参考强平价格
 *   		"markPrice": "6679.50671178",	// 当前标记价格
 *   		"maxNotionalValue": "20000000", // 当前杠杆倍数允许的名义价值上限
 *   		"positionAmt": "0.000", // 头寸数量，符号代表多空方向, 正数为多，负数为空
 *   		"notional": "0",
 *   		"isolatedWallet": "0",
 *   		"symbol": "BTCUSDT", // 交易对
 *   		"unRealizedProfit": "0.00000000", // 持仓未实现盈亏
 *   		"positionSide": "BOTH", // 持仓方向
 *   		"updateTime": 1625474304765   // 更新时间
 *   	    }
 *
 *   	            {
 *         "symbol": "ADAUSDT",
 *         "positionSide": "BOTH",               // 持仓方向
 *         "positionAmt": "30",
 *         "entryPrice": "0.385",
 *         "breakEvenPrice": "0.385077",
 *         "markPrice": "0.41047590",
 *         "unRealizedProfit": "0.76427700",     // 持仓未实现盈亏
 *         "liquidationPrice": "0",
 *         "isolatedMargin": "0",
 *         "notional": "12.31427700",
 *         "marginAsset": "USDT",
 *         "isolatedWallet": "0",
 *         "initialMargin": "0.61571385",        // 初始保证金
 *         "maintMargin": "0.08004280",          // 维持保证金
 *         "positionInitialMargin": "0.61571385",// 仓位初始保证金
 *         "openOrderInitialMargin": "0",        // 订单初始保证金
 *         "adl": 2,
 *         "bidNotional": "0",
 *         "askNotional": "0",
 *         "updateTime": 1720736417660           // 更新时间
 *   }
 */
@Data
public class UMPositionItem {
    private BigDecimal leverage;// 当前杠杆倍数
    private String symbol;
    private String notional;
    private String isolatedWallet;
    private BigDecimal breakEvenPrice;//盈亏平衡价
    private BigDecimal isolatedMargin;
    private String positionSide;
    private BigDecimal liquidationPrice;
    private BigDecimal maxNotionalValue;
    private Long updateTime;
    private BigDecimal entryPrice;// 开仓均价
    private BigDecimal maintMargin;// 维持保证金
    private BigDecimal positionAmt;// 头寸数量，符号代表多空方向, 正数为多，负数为空
    private String isAutoAddMargin;
    private String markPrice;
    private BigDecimal unRealizedProfit;// 持仓未实现盈亏
    private String marginType;
}
