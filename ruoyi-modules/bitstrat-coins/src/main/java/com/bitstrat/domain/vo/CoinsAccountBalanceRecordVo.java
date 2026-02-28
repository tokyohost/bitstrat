package com.bitstrat.domain.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.bitstrat.domain.CoinsAccountBalanceRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 账户余额记录视图对象 coins_account_balance_record
 *
 * @author Lion Li
 * @date 2025-05-07
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsAccountBalanceRecord.class)
public class CoinsAccountBalanceRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long userId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String exchange;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal balance = new  BigDecimal("0");

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal cashBalance = new  BigDecimal("0");

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal usdtBalance = new  BigDecimal("0");

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal freeBalance = new  BigDecimal("0");

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date recordTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date recordDate;

    /**
     * 涨幅
     */
    private BigDecimal growth = new  BigDecimal("0");


    /**
     * 涨幅百分比
     */
    private BigDecimal growthPercentage = new  BigDecimal("0");
}
