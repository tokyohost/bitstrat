package com.bitstrat.domain.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.bitstrat.constant.OrderType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.bitstrat.domain.CoinsBatch;
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
 * 分批订单任务视图对象 coins_batch
 *
 * @author Lion Li
 * @date 2025-04-26
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsBatch.class)
public class CoinsBatchVo implements Serializable {

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
     * 买入交易所
     */
    @ExcelProperty(value = "买入交易所")
    private String buyEx;

    /**
     * 买入总数量
     */
    @ExcelProperty(value = "买入总数量")
    private BigDecimal buyTotal;

    /**
     * 卖出交易所
     */
    @ExcelProperty(value = "卖出交易所")
    private String sellEx;

    /**
     * 卖出总数量
     */
    @ExcelProperty(value = "卖出总数量")
    private BigDecimal sellTotal;

    /**
     * 总批次数量
     */
    @ExcelProperty(value = "总批次数量")
    private Long totalSize;

    /**
     * 总批次
     */
    @ExcelProperty(value = "总批次")
    private Long batchTotal;

    /**
     * 已完成批次
     */
    @ExcelProperty(value = "已完成批次")
    private Integer doneBatch;

    /**
     * 已完成数量
     */
    @ExcelProperty(value = "已完成数量")
    private BigDecimal doneSize;


    /**
     * 状态 10-正在执行 20-执行异常 30-已执行完毕  40-已终止
     */
    @ExcelProperty(value = "状态 10-正在执行 20-执行异常 30-已执行完毕  40-已终止")
    private Long status;

    /**
     * 异常信息
     */
    @ExcelProperty(value = "异常信息")
    private String msg;

    /**
     * 开始时间
     */
    @ExcelProperty(value = "开始时间")
    private Date startTime;

    /**
     * 结束时间
     */
    @ExcelProperty(value = "结束时间")
    private Date endTime;

    /**
     * 用户id
     */
    @ExcelProperty(value = "用户id")
    private Long userId;

    /**
     *操作类型，1-加仓，2-平仓'
     */
    private Integer side;

    /**
     * 每批次操作比例
     */
    private BigDecimal batchSize;

    private BigDecimal doneBuySize;
    private BigDecimal doneSellSize;

    private String symbol;

    /**
     * 下单类型  {@link OrderType}
     */
    private String buyOrderType;
    private String sellOrderType;

    /**
     * 杠杆倍数
     */
    private Integer buyLeverage;
    private Integer sellLeverage;
    /**
     * 机器人Id
     */
    private Long botId;

    private Date createTime;
}
