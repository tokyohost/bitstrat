package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsRankLog;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 山寨币排行日志视图对象 coins_rank_log
 *
 * @author Lion Li
 * @date 2025-04-05
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsRankLog.class)
public class CoinsRankLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 币种id
     */
    @ExcelProperty(value = "币种id")
    private Long rankId;

    /**
     * 得分
     */
    @ExcelProperty(value = "得分")
    private Long score;

    /**
     * 当前市价
     */
    @ExcelProperty(value = "当前市价")
    private String marketPrice;

    /**
     * 涨跌百分比
     */
    @ExcelProperty(value = "涨跌百分比")
    private Long percentage;


}
