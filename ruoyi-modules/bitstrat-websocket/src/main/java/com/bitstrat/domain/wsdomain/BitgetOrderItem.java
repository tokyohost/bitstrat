// BitgetOrderItem.java

// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.wsdomain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BitgetOrderItem {
    private String leverage;
    private String orderType;
    private String tradeSide;
    private String orderId;
    private List<FeeDetail> feeDetail;
    private String cTime;
    private String posMode;
    private String marginMode;
    private String tradeScope;
    private String fillFee;
    private BigDecimal priceAvg;
    private String posSide;
    private String price;
    private String enterPointSource;
    private String baseVolume;
    private String cancelReason;
    private String clientOid;
    private BigDecimal accBaseVolume;
    private String stpMode;
    private String side;
    private String totalProfits;
    private String marginCoin;
    private String notionalUsd;
    private String fillFeeCoin;
    private String fillPrice;
    private String pnl;
    private String fillNotionalUsd;
    private String instId;
    private String presetStopSurplusPrice;
    private String size;
    private String reduceOnly;
    private String presetStopLossPrice;
    private String force;
    private String uTime;
    private String fillTime;
    private String tradeId;
    private String status;

    private Long userId;
}
