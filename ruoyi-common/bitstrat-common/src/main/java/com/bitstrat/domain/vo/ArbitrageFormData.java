// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain.vo;
import com.bitstrat.constant.OrderType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ArbitrageFormData {
    private long leverage;
    private BigDecimal size;
    private long actualSize;
    private double fundingIncome;
    private double fundingRate;

    /**
     * 下单类型  {@link OrderType}
     */
    private String orderType;

    /**
     * 下单账户id
     */
    private Long accountId;

    private String exchange;
    private String symbol;
}
