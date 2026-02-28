package com.bitstrat.domain.wsdomain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/6 17:50
 * @Content
 *{
 *   "id": "450196063_position_1746518134514",
 *   "topic": "position",
 *   "creationTime": 1746518134514,
 *   "data": [
 *     {
 *       "positionIdx": 0,
 *       "tradeMode": 0,
 *       "riskId": 1,
 *       "riskLimitValue": "5000",
 *       "symbol": "DGBUSDT",
 *       "side": "Sell",
 *       "size": "2000",
 *       "entryPrice": "0.008866",
 *       "sessionAvgPrice": "",
 *       "leverage": "1",
 *       "positionValue": "17.732",
 *       "positionBalance": "17.7515052",
 *       "markPrice": "0.008869",
 *       "positionIM": "17.7515052",
 *       "positionMM": "0.3741452",
 *       "takeProfit": "0",
 *       "stopLoss": "0",
 *       "trailingStop": "0",
 *       "unrealisedPnl": "-0.006",
 *       "cumRealisedPnl": "-0.00581167",
 *       "curRealisedPnl": "-0.00581167",
 *       "createdTime": "1746518131033",
 *       "updatedTime": "1746518134512",
 *       "tpslMode": "Full",
 *       "liqPrice": "0.017554",
 *       "bustPrice": "",
 *       "category": "linear",
 *       "positionStatus": "Normal",
 *       "adlRankIndicator": 2,
 *       "autoAddMargin": 0,
 *       "leverageSysUpdatedTime": "",
 *       "mmrSysUpdatedTime": "",
 *       "seq": 204960166472,
 *       "isReduceOnly": false
 *     }
 *   ]
 * }
 */

@Data
public class BybitPositionItem {
    private String symbol;
    private BigDecimal leverage;
    private Long autoAddMargin;
    private BigDecimal liqPrice;
    private String riskLimitValue;
    private String positionValue;
    private String takeProfit;
    private String tpslMode;
    private Boolean isReduceOnly;
    private Long riskId;
    private String trailingStop;
    private BigDecimal unrealisedPnl;
    private String markPrice;
    private Long adlRankIndicator;
    private String cumRealisedPnl;
    private String positionMM;
    private String createdTime;
    private Long positionIdx;
    private String positionIM;
    private Long seq;
    private Long updatedTime;
    private String side;
    private String bustPrice;
    private BigDecimal positionBalance;
    private BigDecimal entryPrice;
    private String leverageSysUpdatedTime;
    private BigDecimal curRealisedPnl;
    private BigDecimal size;
    private String positionStatus;
    private String mmrSysUpdatedTime;
    private String stopLoss;
    private Long tradeMode;
    private String category;
    private BigDecimal sessionAvgPrice;
}
