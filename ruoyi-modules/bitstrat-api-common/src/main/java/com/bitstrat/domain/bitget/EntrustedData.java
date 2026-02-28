// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.bitget;
import java.math.BigDecimal;
import java.util.List;

import com.bitstrat.domain.TpSlOrder;
import lombok.Data;

/**
 * https://www.bitget.com/zh-CN/api-doc/contract/plan/get-orders-plan-pending
 */
@Data
public class EntrustedData  extends TpSlOrder {
    private String symbol;
    private String callbackRatio;
    private String orderType;
    private String tradeSide;
    private String orderId;
    private String planStatus;
    private String posMode;
    private String cTime;
    private String marginMode;
    private BigDecimal stopSurplusTriggerPrice;
    private BigDecimal stopLossTriggerPrice;
    private String posSide;
    private String price;
    private String enterPointSource;
    private String stopLossExecutePrice;
    private String stopSurplusExecutePrice;
    private String side;
    private String orderSource;
    private String planType;
    private String triggerPrice;
    private String stopSurplusTriggerType;
    private String marginCoin;
    private String stopLossTriggerType;
    private String size;
    private String executePrice;
    private String uTime;
    private String triggerType;
    private String clientOid;
}
