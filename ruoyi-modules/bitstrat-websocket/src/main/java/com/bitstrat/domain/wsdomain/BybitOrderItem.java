// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.wsdomain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * {
 *   "topic": "execution",
 *   "id": "450196063_DGBUSDT_204960166472",
 *   "creationTime": 1746518134515,
 *   "data": [
 *     {
 *       "category": "linear",
 *       "symbol": "DGBUSDT",
 *       "closedSize": "0",
 *       "execFee": "0.0017732",
 *       "execId": "282e2f12-770a-5b69-9c03-f9fbdc640f01",
 *       "execPrice": "0.008866",
 *       "execQty": "1000",
 *       "execType": "Trade",
 *       "execValue": "8.866",
 *       "feeRate": "0.0002",
 *       "tradeIv": "",
 *       "markIv": "",
 *       "blockTradeId": "",
 *       "markPrice": "0.008869",
 *       "indexPrice": "",
 *       "underlyingPrice": "",
 *       "leavesQty": "0",
 *       "orderId": "40c2854e-cc3d-4041-8238-8ee296e1603b",
 *       "orderLinkId": "1919662304632356865",
 *       "orderPrice": "0.008866",
 *       "orderQty": "1000",
 *       "orderType": "Limit",
 *       "stopOrderType": "UNKNOWN",
 *       "side": "Sell",
 *       "execTime": "1746518134510",
 *       "isLeverage": "0",
 *       "isMaker": true,
 *       "seq": 204960166472,
 *       "marketUnit": "",
 *       "execPnl": "0",
 *       "createType": "CreateByUser"
 *     }
 *   ]
 * }
 */

@Data
public class BybitOrderItem {
    private String symbol;
    private String underlyingPrice;
    private String orderType;
    private String orderLinkId;
    private String orderId;
    private BigDecimal execPnl;
    private String stopOrderType;
    private String execTime;
    private String createType;
    private String feeRate;
    private String tradeIv;
    private String blockTradeId;
    private String markPrice;
    private String isLeverage;
    private String execPrice;
    private String markIv;
    private BigDecimal orderQty;
    private String orderPrice;
    private String closedSize;
    private BigDecimal execValue;
    private String execType;
    private Long seq;
    private String side;
    private String indexPrice;
    private BigDecimal leavesQty;
    private Boolean isMaker;
    private BigDecimal execFee;
    private UUID execId;
    private String marketUnit;
    private String category;
    private BigDecimal execQty;
}
