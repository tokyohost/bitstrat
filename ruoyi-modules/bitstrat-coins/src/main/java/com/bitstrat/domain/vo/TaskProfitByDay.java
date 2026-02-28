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
public class TaskProfitByDay {
    private String day;
    private BigDecimal profit;

    private TaskAnalysisByDay dayAnalysis;
}
