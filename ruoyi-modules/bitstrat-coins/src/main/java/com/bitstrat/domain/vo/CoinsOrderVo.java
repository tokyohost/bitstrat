package com.bitstrat.domain.vo;

import com.bitstrat.constant.OrderType;
import com.bitstrat.domain.CoinsOrder;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 订单列表视图对象 coins_order
 *
 * @author Lion Li
 * @date 2025-04-21
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsOrder.class)
public class CoinsOrderVo implements Serializable {

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
     * 订单id
     */
    @ExcelProperty(value = "订单id")
    private String orderId;

    /**
     * 交易所名称
     */
    @ExcelProperty(value = "交易所名称")
    private String ex;

    /**
     * 币对
     */
    @ExcelProperty(value = "币对")
    private String symbol;

    /**
     * 数量
     */
    @ExcelProperty(value = "数量")
    private String size;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态")
    private String status;

    /**
     * 手续费
     */
    @ExcelProperty(value = "手续费")
    private String fee;

    /**
     * 平均价格
     */
    @ExcelProperty(value = "平均价格")
    private String avgPrice;

    /**
     * 下单价格
     */
    @ExcelProperty(value = "下单价格")
    private String price;

    private String side;

    /**
     * 订单是否已经是终结态  1是 0否
     */
    private Integer orderEnd;

    /**
     * 剩余未成交的数量
     */
    BigDecimal leavesQty;
    /**
     * 剩余未成交的价值
     */
    BigDecimal leavesValue;
    /**
     * 累计已成交的价值
     */
    BigDecimal cumExecValue;
    /**
     * 累计已成交的数量
     */
    BigDecimal cumExecQty;
    Long createBy;

    /**
     * 市价
     */
    BigDecimal marketPrice;

    /**
     * 是否是平仓单 1-是 0-不是
     */
    Long closePositionOrder;

    /**
     * 下单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Date updateTime;
    /**
     * 订单类型 {@link OrderType}
     */
    String orderType;

    Long batchId;

    Integer batchCount;

    /**
     * 每笔平仓盈亏
     */
    BigDecimal pnl;

    Long batchTotal;

    Long accountId;

    String abTaskId;
}
