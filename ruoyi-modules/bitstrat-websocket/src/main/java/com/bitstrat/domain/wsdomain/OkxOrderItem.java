// OkxOrderItem.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.wsdomain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OkxOrderItem {
    private String msg;
    private String pxType;
    private BigDecimal fee;
    private BigDecimal px;
    private String tpTriggerPxType;
    private String source;
    private String ordId;
    private String attachAlgoClOrdId;
    private String fillFee;
    private String clOrdId;
    private String ccy;
    private String fillMarkVol;
    private LinkedAlgoOrd linkedAlgoOrd;
    private String tag;
    private String state;
    private String quickMgnType;
    private List<Object> attachAlgoOrds;
    private String stpId;
    private String slTriggerPxType;
    private String fillPnl;
    private String notionalUsd;
    private String tgtCcy;
    private String tdMode;
    private String lever;
    private String tpOrdPx;
    private BigDecimal pnl;
    private String reqId;
    private String instType;
    private String fillPxUsd;
    private String fillNotionalUsd;
    private String fillFeeCcy;
    private String reduceOnly;
    private String slOrdPx;
    private String amendSource;
    private String pxUsd;
    private String ordType;
    private String fillSz;
    private String fillPxVol;
    private String code;
    private String fillFwdPx;
    private String pxVol;
    private String algoClOrdId;
    private String cTime;
    private String tpTriggerPx;
    private BigDecimal accFillSz;
    private String posSide;
    private String isTpLimit;
    private String execType;
    private String fillIdxPx;
    private String stpMode;
    private String side;
    private String fillPx;
    private String amendResult;
    private String algoId;
    private String rebate;
    private String lastPx;
    private BigDecimal sz;
    private String instId;
    private BigDecimal avgPx;
    private String cancelSource;
    private String slTriggerPx;
    private String uTime;
    private String fillTime;
    private String rebateCcy;
    private String category;
    private String tradeId;
    private String fillMarkPx;
    private String feeCcy;
}

// LinkedAlgoOrd.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

@Data
class LinkedAlgoOrd {
    private String algoId;
}
