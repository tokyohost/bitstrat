package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsAbTask;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 价差套利任务视图对象 coins_ab_task
 *
 * @author Lion Li
 * @date 2025-06-08
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsAbTask.class)
public class CoinsAbTaskVo implements Serializable {

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
    private String taskId;

    /**
     * 任务体
     */
    @ExcelProperty(value = "任务体")
    private String body;

    /**
     * 用户id
     */
    @ExcelProperty(value = "用户id")
    private Long userId;


}
