package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsRank;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 山寨币排行视图对象 coins_rank
 *
 * @author Lion Li
 * @date 2025-04-05
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsRank.class)
public class CoinsRankVo implements Serializable {

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
    private Double score;

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
    private String tenantId;

    private Date createTime;

    private Date updateTime;
    private Integer rank;
    private String historyRecord;
}
