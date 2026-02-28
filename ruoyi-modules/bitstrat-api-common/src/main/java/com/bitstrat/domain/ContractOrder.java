package com.bitstrat.domain;

import com.bitstrat.constant.ContractOrderStatus;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/21 18:05
 * @Content
 */

@Data
public class ContractOrder {
    String orderId;
    //exchange 交易所
    String ex;

    /**
     * 订单状态
     * {@link ContractOrderStatus}
     */
    String status;

    /**
     * 订单手续费
     */
    BigDecimal fee;

    BigDecimal avgPrice;

    //订单价格
    BigDecimal price;

    //订单方向

    /**
     * {@link com.bitstrat.constant.SideType}
     */

    String side;

    /**
     * 数量
     */
    BigDecimal size;

    /**
     * 剩余未成交的数量
     */
    BigDecimal leavesQty;
    /**
     * 剩余未成交的价值
     */
    BigDecimal leavesValue;
    /**
     * 累计已成交的价值
     */
    BigDecimal cumExecValue;
    /**
     * 累计已成交的数量
     */
    BigDecimal cumExecQty;

    /**
     * 平仓盈亏
     */
    BigDecimal pnl;
    //订单是否结束
    Boolean orderEnd;
}
