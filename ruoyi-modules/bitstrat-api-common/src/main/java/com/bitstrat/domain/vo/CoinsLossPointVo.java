package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsLossPoint;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 滑点管理视图对象 coins_loss_point
 *
 * @author Lion Li
 * @date 2025-04-11
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsLossPoint.class)
public class CoinsLossPointVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 交易所名称
     */
    @ExcelProperty(value = "交易所名称")
    private String exchangeName;

    /**
     * 币种
     */
    @ExcelProperty(value = "币种")
    private String symbol;

    /**
     * 价格
     */
    @ExcelProperty(value = "价格")
    private BigDecimal price;

    /**
     * 回撤率
     */
    @ExcelProperty(value = "回撤率")
    private BigDecimal retread;

    /**
     * 下单数量
     */
    @ExcelProperty(value = "下单数量")
    private BigDecimal quantity;
    private BigDecimal triggerPrice1;
    private BigDecimal triggerPrice2;
    private Integer status;

    private String nodeName;

    private String nodeClientId;
    private Integer enable;

    private Long createBy;

    private BigDecimal stopLossCalcLimit;
}
