// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.bitget;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class BitgetOrderDetail {
    private String symbol;
    private String orderType;
    private BigDecimal leverage;
    private String tradeSide;
    private String orderId;
    private BigDecimal fee;
    private String posMode;
    private String cTime;
    private String marginMode;
    private BigDecimal priceAvg;
    private String posSide;
    private BigDecimal price;
    private String enterPointSource;
    private String state;
    private BigDecimal baseVolume;
    private String cancelReason;
    private String side;
    private String orderSource;
    private String totalProfits;
    private String marginCoin;
    private String quoteVolume;
    private BigDecimal presetStopSurplusPrice;
    private BigDecimal size;
    private String reduceOnly;
    private BigDecimal presetStopLossPrice;
    private String force;
    private String uTime;
    private String clientOid;
}
