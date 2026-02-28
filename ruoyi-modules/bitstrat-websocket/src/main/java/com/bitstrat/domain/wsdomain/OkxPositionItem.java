// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.wsdomain;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OkxPositionItem {
    private String spotInUseAmt;
    private String gammaPA;
    private String nonSettleAvgPx;
    private String bePx;
    private String bizRefType;
    private BigDecimal fee;
    private String usdPx;
    private String liabCcy;
    private String pTime;
    private String availPos;
    private String quoteInterest;
    private BigDecimal settledPnl;
    private String ccy;
    private String baseInterest;
    private String uplRatioLastPx;
    private BigDecimal liqPx;
    private String thetaPA;
    private BigDecimal margin;
    private String last;
    private String optVal;
    private String adl;
    private String mgnMode;
    private String notionalUsd;
    private BigDecimal lever;
    private String quoteBorrowed;
    private String liqPenalty;
    private String pendingCloseOrdLiabVal;
    private String pnl;
    private String instType;
    private BigDecimal upl;
    private String bizRefId;
    private String markPx;
    private BigDecimal realizedPnl;
    private BigDecimal mgnRatio;
    private String vegaBS;
    private String cTime;
    private String baseBorrowed;
    private String clSpotInUseAmt;
    private String imr;
    private String baseBal;
    private String posId;
    private String spotInUseCcy;
    private String idxPx;
    private BigDecimal mmr;
    /**
     * 持仓方向
     * long：开平仓模式开多
     * short：开平仓模式开空
     * net：买卖模式（交割/永续/期权：pos为正代表开多，pos为负代表开空。币币杠杆：posCcy为交易货币时，代表开多；posCcy为计价货币时，代表开空。）
     */
    private String posSide;
    private String Integererest;
    private BigDecimal pos;
    private String posCcy;
    private String deltaPA;
    private String vegaPA;
    private String gammaBS;
    private String uplLastPx;
    private String quoteBal;
    private BigDecimal fundingFee;
    private String liab;
    private String instId;
    @JSONField(name = "closeOrderAlgo")
    private List<OkxOrderAlgo> closeOrderAlgo;
    private BigDecimal avgPx;
    private String maxSpotInUseAmt;
    private String deltaBS;
    private Long uTime;
    private String uplRatio;
    private String tradeId;
    private String thetaBS;

    private BigDecimal takeProfit;
    private BigDecimal stopLoss;
}
