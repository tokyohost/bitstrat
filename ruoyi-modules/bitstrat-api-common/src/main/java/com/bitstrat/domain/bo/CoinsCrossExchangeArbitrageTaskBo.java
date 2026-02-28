package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsCrossExchangeArbitrageTask;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 跨交易所套利任务业务对象 coins_cross_exchange_arbitrage_task
 *
 * @author Lion Li
 * @date 2025-04-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsCrossExchangeArbitrageTask.class, reverseConvertGenerate = false)
public class CoinsCrossExchangeArbitrageTaskBo extends BaseEntity {

    /**
     * id
     */
    private Long id;

    /**
     * 做多交易所
     */
    private String longEx;

    /**
     * 做多币对
     */
    private String longSymbol;

    /**
     * 做多金额 USDT
     */
    private BigDecimal longSize;

    /**
     * 做多持仓数量
     */
    private BigDecimal longSymbolSize;

    /**
     * 做多入场价
     */
    private String longAvgPrice;

    /**
     * 做多出场价
     */
    private String longOutPrice;

    /**
     * 做多盈亏 USDT
     */
    private BigDecimal longProfit;

    /**
     * 做多入场时间
     */
    private Date longInTime;

    /**
     * 做多杠杆倍数
     */
    private Long longLeverage;

    /**
     * 做多开仓手续费
     */
    private BigDecimal longInFee;

    /**
     * 做多平仓手续费
     */
    private BigDecimal longOutFee;

    /**
     * 做空交易所
     */
    private String shortEx;

    /**
     * 做空币对
     */
    private String shortSymbol;

    /**
     * 做空金额 USDT
     */
    private BigDecimal shortSize;

    /**
     * 做空持仓数量
     */
    private BigDecimal shortSymbolSize;

    /**
     * 做空入场价
     */
    private String shortAvgPrice;

    /**
     * 做空出场价
     */
    private String shortOutPrice;

    /**
     * 做空盈亏 USDT
     */
    private BigDecimal shortProfit;

    /**
     * 做空入场时间
     */
    private Date shortInTime;

    /**
     * 做空杠杆倍数
     */
    private Long shortLeverage;

    /**
     * 做空开仓手续费
     */
    private BigDecimal shortInFee;

    /**
     * 做空平仓手续费
     */
    private BigDecimal shortOutFee;

    /**
     * 执行节点clientId
     */
    private String excuteNodeId;

    /**
     * 总盈亏 USDT
     */
    private BigDecimal totalProfit;

    /**
     * 状态 1-正在运行 2-已停止 3-未启动
     */
    private Long status;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 任务开始时间戳
     */
    private Long startTime;

    private String symbol;

    private Date createTime;

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

    private String longPosId;
    private String shortPosId;

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

    private Long longAccountId;
    private Long shortAccountId;

    /**
     * 所属机器人id
     */
    private Long botId;

}
