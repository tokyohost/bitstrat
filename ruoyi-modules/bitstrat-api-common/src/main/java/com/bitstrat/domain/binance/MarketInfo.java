// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.binance;
import lombok.Data;

import java.math.BigDecimal;

/**
 * {
 *     "symbol": "BTCUSDT",				// 交易对
 *     "markPrice": "11793.63104562",		// 标记价格
 *     "indexPrice": "11781.80495970",		// 指数价格
 *     "estimatedSettlePrice": "11781.16138815",  // 预估结算价,仅在交割开始前最后一小时有意义
 *     "lastFundingRate": "0.00038246",	// 最近更新的资金费率
 *     "interestRate": "0.00010000",		// 标的资产基础利率
 *     "nextFundingTime": 1597392000000,	// 下次资金费时间
 *     "time": 1597370495002				// 更新时间
 * }
 */
@Data
public class MarketInfo {
    private String IntegererestRate;
    private String symbol;
    private BigDecimal markPrice;
    private BigDecimal indexPrice;
    private BigDecimal lastFundingRate;
    private Long nextFundingTime;
    private Long time;
    private BigDecimal estimatedSettlePrice;
}
