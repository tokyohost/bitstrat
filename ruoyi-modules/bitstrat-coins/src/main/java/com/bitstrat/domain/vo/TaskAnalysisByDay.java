package com.bitstrat.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/23 19:03
 * @Content
 */

@Data
public class TaskAnalysisByDay {
    private String day;
    //盈亏比
    private BigDecimal plRatio;

    private BigDecimal profit;

    // 多空比
    private BigDecimal lsRatio;

    //胜率
    private BigDecimal winRatio;

    private BigDecimal totalFee;

    private BigDecimal fundingFee;
    //平均持仓时长
    private BigDecimal avgHoldHours;

    private Integer winCount;
    private Integer loseCount;

    private Integer longCount;
    private Integer shortCount;
}
