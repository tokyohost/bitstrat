// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

import java.util.UUID;

@Data
public class BybitOrderCloseItem {
    private String symbol;
    private String orderType;
    private String leverage;
    private String updatedTime;
    private String side;
    private String orderId;
    private BigDecimal closedPnl;
    private String avgEntryPrice;
    private String qty;
    private String cumEntryValue;
    private String createdTime;
    private String orderPrice;
    private String closedSize;
    private String avgExitPrice;
    private String execType;
    private String fillCount;
    private String cumExitValue;
}
