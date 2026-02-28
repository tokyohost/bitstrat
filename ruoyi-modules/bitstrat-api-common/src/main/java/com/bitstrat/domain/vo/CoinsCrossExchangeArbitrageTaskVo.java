package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsCrossExchangeArbitrageTask;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 * 跨交易所套利任务视图对象 coins_cross_exchange_arbitrage_task
 *
 * @author Lion Li
 * @date 2025-04-19
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsCrossExchangeArbitrageTask.class)
public class CoinsCrossExchangeArbitrageTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 做多交易所
     */
    @ExcelProperty(value = "做多交易所")
    private String longEx;

    /**
     * 做多币对
     */
    @ExcelProperty(value = "做多币对")
    private String longSymbol;

    /**
     * 做多金额 USDT
     */
    @ExcelProperty(value = "做多金额 USDT")
    private BigDecimal longSize;

    /**
     * 做多持仓数量
     */
    @ExcelProperty(value = "做多持仓数量")
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal longSymbolSize;

    /**
     * 做多入场价
     */
    @ExcelProperty(value = "做多入场价")
    private String longAvgPrice;

    /**
     * 做多出场价
     */
    @ExcelProperty(value = "做多出场价")
    private String longOutPrice;

    /**
     * 做多盈亏 USDT
     */
    @ExcelProperty(value = "做多盈亏 USDT")
    private BigDecimal longProfit;

    /**
     * 做多入场时间
     */
    @ExcelProperty(value = "做多入场时间")
    private Date longInTime;

    /**
     * 做多杠杆倍数
     */
    @ExcelProperty(value = "做多杠杆倍数")
    private Long longLeverage;

    /**
     * 做多开仓手续费
     */
    @ExcelProperty(value = "做多开仓手续费")
    private BigDecimal longInFee;

    /**
     * 做多平仓手续费
     */
    @ExcelProperty(value = "做多平仓手续费")
    private BigDecimal longOutFee;

    /**
     * 做空交易所
     */
    @ExcelProperty(value = "做空交易所")
    private String shortEx;

    /**
     * 做空币对
     */
    @ExcelProperty(value = "做空币对")
    private String shortSymbol;

    /**
     * 做空金额 USDT
     */
    @ExcelProperty(value = "做空金额 USDT")
    private BigDecimal shortSize;

    /**
     * 做空持仓数量
     */
    @ExcelProperty(value = "做空持仓数量")
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal shortSymbolSize;

    /**
     * 做空入场价
     */
    @ExcelProperty(value = "做空入场价")
    private String shortAvgPrice;

    /**
     * 做空出场价
     */
    @ExcelProperty(value = "做空出场价")
    private String shortOutPrice;

    /**
     * 做空盈亏 USDT
     */
    @ExcelProperty(value = "做空盈亏 USDT")
    private BigDecimal shortProfit;

    /**
     * 做空入场时间
     */
    @ExcelProperty(value = "做空入场时间")
    private Date shortInTime;

    /**
     * 做空杠杆倍数
     */
    @ExcelProperty(value = "做空杠杆倍数")
    private Long shortLeverage;

    /**
     * 做空开仓手续费
     */
    @ExcelProperty(value = "做空开仓手续费")
    private BigDecimal shortInFee;

    /**
     * 做空平仓手续费
     */
    @ExcelProperty(value = "做空平仓手续费")
    private BigDecimal shortOutFee;

    /**
     * 执行节点clientId
     */
    @ExcelProperty(value = "执行节点clientId")
    private String excuteNodeId;

    /**
     * 总盈亏 USDT
     */
    @ExcelProperty(value = "总盈亏 USDT")
    private BigDecimal totalProfit;

    /**
     * 状态 1-正在运行 2-已停止 3-未启动
     */
    @ExcelProperty(value = "状态 1-正在运行 2-已停止 3-未启动")
    private Long status;

    /**
     * 用户id
     */
    @ExcelProperty(value = "用户id")
    private Long userId;

    /**
     * 任务开始时间戳
     */
    @ExcelProperty(value = "任务开始时间戳")
    private Long startTime;

    private String symbol;

    private Date createTime;
    private Date updateTime;
    private Integer batchIncome;
    private Double batchPrice;

    private Integer batchPosition;
    private Integer batchSize;
    private String role;

    /**
     * 累计资金费
     */
    private BigDecimal shortFundingFee;
    private BigDecimal longFundingFee;

    /**
     * 保证金率
     */
    private BigDecimal longMgnRatio;


    /**
     * 保证金率
     */
    private BigDecimal shortMgnRatio;
    /**
     * 做多预估强平价
     */
    private BigDecimal longLiqPx;
    /**
     * 做空预估强平价
     */
    private BigDecimal shortLiqPx;

    /**
     * 做多维持保证金率
     */
    private BigDecimal longMarginRatio;

    /**
     * 做空维持保证金率
     */
    private BigDecimal shortMarginRatio;

    private String longPosId;
    private String shortPosId;

    /**
     * 告警阈值
     */
    private BigDecimal warningThreshold;

    /**
     * 年化收益率
     */
    private BigDecimal apy;

    private Long longAccountId;
    private Long shortAccountId;

    /**
     * 所属机器人id
     */
    private Long botId;


    /**
     * 爆仓预警状态
     */
    private Integer liquidationConfigStatus;

    /**
     * 做多爆仓预警阈值
     */
    private BigDecimal longLiquidationThreshold;

    /**
     * 做空爆仓预警阈值
     */
    private BigDecimal shortLiquidationThreshold;
}
