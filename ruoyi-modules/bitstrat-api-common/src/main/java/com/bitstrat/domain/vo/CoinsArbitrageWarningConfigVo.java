package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsArbitrageWarningConfig;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 用户配置套利警告视图对象 coins_arbitrage_warning_config
 *
 * @author Lion Li
 * @date 2025-05-04
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsArbitrageWarningConfig.class)
public class CoinsArbitrageWarningConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 跨交易所套利任务ID
     */
    @ExcelProperty(value = "跨交易所套利任务ID")
    private Long taskId;

    /**
     * 套利类型：1:跨交易所套利，2:。。。详见枚举类
     */
    @ExcelProperty(value = "套利类型：1:跨交易所套利，2:。。。详见枚举类")
    private Long arbitrageType;

    /**
     * 警告阈值
     */
    @ExcelProperty(value = "警告阈值")
    private BigDecimal warningThreshold;

    /**
     * 告警配置名称
     */
    @ExcelProperty(value = "告警配置名称")
    private String configName;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 用户id
     */
    @ExcelProperty(value = "用户id")
    private Long userId;

    /**
     * 状态，1-正常，0-停用
     */
    @ExcelProperty(value = "状态")
    private Integer status = 0;

    /**
     * 做多交易所
     */
    private String longEx;
    /**
     * 做空交易所
     */
    private String shortEx;
    /**
     * 币对
     */
    private String symbol;


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
