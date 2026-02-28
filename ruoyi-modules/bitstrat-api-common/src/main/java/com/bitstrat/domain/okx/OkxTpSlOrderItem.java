// OkxOrder.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.okx;
import com.bitstrat.domain.TpSlOrder;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OkxTpSlOrderItem extends TpSlOrder {
    private String callbackRatio;
    private String advanceOrdType;
    private String maxChaseType;
    private String tpTriggerPxType;
    private String ordId;
    private String isTradeBorrowMode;
    private String clOrdId;
    private String closeFraction;
    private String ccy;
    private String failCode;
    private String ordPx;
    private String state;
    private String tag;
    private String pxVar;
    private String quickMgnType;
    private String maxChaseVal;
    private String slTriggerPxType;
    private String last;
    private String callbackSpread;
    private String lever;
    private String tdMode;
    private String tgtCcy;
    private String tpOrdPx;
    private String actualSide;
    private String instType;
    private String triggerPx;
    private String reduceOnly;
    private String slOrdPx;
    private String triggerPxType;
    private String ordType;
    private String tradeQuoteCcy;
    private String algoClOrdId;
    private String cTime;
    private String pxLimit;
    private String triggerTime;
    private BigDecimal tpTriggerPx;
    private String posSide;
    private String timeInterval;
    private String actualPx;
    private String amendPxOnTriggerType;
    private String side;
    private String activePx;
    private String algoId;
    private String sz;
    private String szLimit;
    private String chaseType;
    private String pxSpread;
    private String instId;
    private String moveTriggerPx;
    private String chaseVal;
    private BigDecimal slTriggerPx;
    private String uTime;
    private String actualSz;
}

