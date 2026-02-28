// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.binance;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * {
 *   	"buyer": false,	// 是否是买方
 *   	"commission": "-0.07819010", // 手续费
 *   	"commissionAsset": "USDT", // 手续费计价单位
 *   	"id": 698759,	// 交易ID
 *   	"maker": false,	// 是否是挂单方
 *   	"orderId": 25851813, // 订单编号
 *   	"price": "7819.01",	// 成交价
 *   	"qty": "0.002",	// 成交量
 *   	"quoteQty": "15.63802",	// 成交额
 *   	"realizedPnl": "-0.91539999",	// 实现盈亏
 *   	"side": "SELL",	// 买卖方向
 *   	"positionSide": "SHORT",  // 持仓方向
 *   	"symbol": "BTCUSDT", // 交易对
 *   	"time": 1569514978020 // 时间
 *   }
 */
@Data
public class AccountTradeListItem {
    private String symbol;
    private String side;
    private BigDecimal quoteQty;// 成交额
    private Long orderId;
    private String positionSide;
    private Boolean maker;
    private String commissionAsset;
    private Boolean buyer;
    private BigDecimal price;// 成交价
    private BigDecimal qty;// 成交量
    private BigDecimal realizedPnl;// 实现盈亏
    private BigDecimal commission;// 手续费
    private Long id;
    private Long time;
}
