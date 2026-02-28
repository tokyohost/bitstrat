package com.bitstrat.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderPosition {
    private String symbol;
    //持仓反向  long 做多/ short 做空
    private String side;
    //平均持仓价
    private BigDecimal avgPrice;
    //持仓数量
    private BigDecimal size;
    //盈亏
    private BigDecimal profit;

    //持仓id okx 会有
    private String positionId;

    private BigDecimal marketPrice;

    /**
     * 已实现收益
     */
    private BigDecimal realizedPnl;
    /**
     * 已结算收益
     */
    private BigDecimal settledPnl;

    /**
     * 累计手续费
     */
    private BigDecimal fee;
    /**
     * 累计资金费
     */
    private BigDecimal fundingFee;

    /**
     * 杠杆倍数
     */
    private BigDecimal lever;

    /**
     * 保证金率
     */
    private BigDecimal mgnRatio;

    /**
     * 预估强平价
     */
    private BigDecimal liqPx;

    /**
     * 盈亏平衡价
     */
    private BigDecimal breakEvenPrice;
    private String ex;

    /**
     * 冗余字段，所属那个任务
     */
    private Long taskId;

    private Long accountId;

}
