package com.bitstrat.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import com.bitstrat.domain.CoinsFinancialFlowRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Builder;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 交易所资金流水记录视图对象 coins_financial_flow_record
 *
 * @author Lion Li
 * @date 2025-06-02
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsFinancialFlowRecord.class)
public class CoinsFinancialFlowRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 交易所原始流水ID
     */
    @ExcelProperty(value = "交易所原始流水ID")
    private String exchangeRecordId;

    /**
     * 交易所名称
     */
    @ExcelProperty(value = "交易所名称")
    private String exchange;

    /**
     * 资金流水类型
     */
    @ExcelProperty(value = "资金流水类型")
    private String flowType;

    /**
     * 交易对
     */
    @ExcelProperty(value = "交易对")
    private String symbol;

    /**
     * 流水发生时间
     */
    @ExcelProperty(value = "流水发生时间")
    private LocalDateTime timestamp;

    /**
     * 金额(正数为收入/负数为支出)
     */
    @ExcelProperty(value = "金额(正数为收入/负数为支出)")
    private BigDecimal amount;

    /**
     * 资产类型
     */
    @ExcelProperty(value = "资产类型")
    private String asset;


}
