package com.bitstrat.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.bitstrat.domain.CoinApiPosition;
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
 * API 历史仓位数据视图对象 coin_api_position
 *
 * @author Lion Li
 * @date 2025-12-29
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinApiPosition.class)
public class CoinApiPositionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * api id
     */
    @ExcelProperty(value = "api id")
    private Long apiId;

    /**
     * 用户id
     */
    @ExcelProperty(value = "用户id")
    private Long userId;

    /**
     * 仓位ID
     */
    @ExcelProperty(value = "仓位ID")
    private String positionId;

    /**
     * 币对
     */
    @ExcelProperty(value = "币对")
    private String coin;

    /**
     * 方向
     */
    @ExcelProperty(value = "方向")
    private String side;

    /**
     * 数量
     */
    @ExcelProperty(value = "数量")
    private BigDecimal size;

    /**
     * 开仓价
     */
    @ExcelProperty(value = "开仓价")
    private BigDecimal open;

    /**
     * 平仓价
     */
    @ExcelProperty(value = "平仓价")
    private BigDecimal close;

    /**
     * 仓位类型
     */
    @ExcelProperty(value = "仓位类型")
    private String marginMode;

    /**
     * 盈亏净值
     */
    @ExcelProperty(value = "盈亏净值")
    private BigDecimal netProfit;

    /**
     * pnl
     */
    @ExcelProperty(value = "pnl")
    private BigDecimal pnl;

    /**
     * 资金费
     */
    @ExcelProperty(value = "资金费")
    private BigDecimal totalFunding;

    /**
     * 开仓手续费
     */
    @ExcelProperty(value = "开仓手续费")
    private BigDecimal openFee;

    /**
     * 平仓手续费
     */
    @ExcelProperty(value = "平仓手续费")
    private BigDecimal closeFee;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private String utime;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private String ctime;

    /**
     * 时间戳
     */
    @ExcelProperty(value = "时间戳")
    private Date time;


}
