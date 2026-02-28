package com.bitstrat.domain;

import com.alibaba.fastjson2.annotation.JSONField;
import com.bitstrat.constant.PositionType;
import com.bitstrat.constant.SideType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class PositionWsData {
    private String exchange;

    /**
     * 仓位类型
     * see {@link PositionType}
     */
    private String posType;


    private String symbol;
    private BigDecimal size;

    /**
     * see {@link SideType}
     */
    private String side;
    private BigDecimal avgPrice;
    /**
     * 资金费
     */
    private BigDecimal fundingFee;

    /**
     * 手续费
     */
    private BigDecimal fee;
    /**
     * 盈亏
     */
    private BigDecimal profit;
    private BigDecimal achievedProfits;
    private BigDecimal unrealizedPL;
    private BigDecimal totalFee;
    private BigDecimal keepMarginRate;

    /**
     * 未实现盈亏
     */
    private BigDecimal unrealizedProfit;

    /**
     * 保证金模式
     */
    private String marginType;

    /**
     * 保证金
     */
    private BigDecimal marginPrice;
    /**
     * 保证金率
     */
    private BigDecimal marginRatio;
    /**
     * api名称
     */
    private String accountName;

    /**
     * 更新时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * server时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date serverTime;

    /**
     * 杠杆倍数
     */
    private BigDecimal leverage;

    /**
     * 预估强平价
     */
    private BigDecimal liqPrice;

    /**
     * 是否平仓
     */
    private Boolean closed;

    private Long accountId;


    //止盈价格
    private BigDecimal takeProfit;

    //止损价格
    private BigDecimal stopLoss;

    private String holdSide;

    //止盈止损单
    private List<? extends TpSlOrder> tpSlOrders;
}
