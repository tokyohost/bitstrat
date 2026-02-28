package com.bitstrat.domain.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.bitstrat.domain.CoinAITaskBalance;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * AI 测试趋势视图对象 coin_test_ai
 *
 * @author Lion Li
 * @date 2025-10-29
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinAITaskBalance.class)
public class CoinAITaskBalanceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 金额
     */
    @ExcelProperty(value = "金额")
    private BigDecimal equity;

    /**
     * 可用
     */
    @ExcelProperty(value = "可用")
    private BigDecimal freeBalance;

    /**
     * 时间戳
     */
    @ExcelProperty(value = "时间戳")
    private Date time;

    private Long taskId;
}
