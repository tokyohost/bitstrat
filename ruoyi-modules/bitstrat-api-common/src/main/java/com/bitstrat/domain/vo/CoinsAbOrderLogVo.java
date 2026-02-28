package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsAbOrderLog;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 价差套利日志视图对象 coins_ab_order_log
 *
 * @author Lion Li
 * @date 2025-06-08
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsAbOrderLog.class)
public class CoinsAbOrderLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * account a
     */
    @ExcelProperty(value = "account a")
    private Long accountA;

    /**
     * account b
     */
    @ExcelProperty(value = "account b")
    private Long accountB;

    /**
     * exchangea
     */
    @ExcelProperty(value = "exchangea")
    private String exchangeA;

    /**
     * exchangeb
     */
    @ExcelProperty(value = "exchangeb")
    private String exchangeB;

    /**
     * TaskId
     */
    @ExcelProperty(value = "TaskId")
    private String taskId;

    /**
     * 日志
     */
    @ExcelProperty(value = "日志")
    private String log;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;


}
