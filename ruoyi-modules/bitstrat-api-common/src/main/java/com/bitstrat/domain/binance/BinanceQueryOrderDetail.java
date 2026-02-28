// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.binance;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class BinanceQueryOrderDetail {
    private String symbol;
    private BigDecimal executedQty;
    private String priceRate;
    private Long orderId;
    private Long goodTillDate;
    private BigDecimal avgPrice;
    private String type;
    private Boolean priceProtect;
    private BigDecimal price;
    private String timeInForce;
    private String side;
    private BigDecimal origQty;
    private String clientOrderId;
    private String positionSide;
    private String activatePrice;
    private Long updateTime;
    private Boolean closePosition;
    private String origType;
    private String stopPrice;
    private Boolean reduceOnly;
    private String cumQuote;
    private String selfTradePreventionMode;
    private Long time;
    private String workingType;
    private String status; // 订单状态
    private String priceMatch;
}
