package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsRankReversed;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 山寨币排行(反向)视图对象 coins_rank_reversed
 *
 * @author Lion Li
 * @date 2025-04-06
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsRankReversed.class)
public class CoinsRankReversedVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 币种
     */
    @ExcelProperty(value = "币种")
    private String symbol;

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
    private Double percentage;

    /**
     * 排名
     */
    @ExcelProperty(value = "排名")
    private Long rank;
    private String tenantId;

    private Date createTime;

    private Date updateTime;
    private String historyRecord;
}
