package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsCrossTaskLog;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 跨交易所任务日志视图对象 coins_cross_task_log
 *
 * @author Lion Li
 * @date 2025-04-19
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsCrossTaskLog.class)
public class CoinsCrossTaskLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 任务id
     */
    @ExcelProperty(value = "任务id")
    private Long taskId;

    /**
     * 日志
     */
    @ExcelProperty(value = "日志")
    private String msg;


}
