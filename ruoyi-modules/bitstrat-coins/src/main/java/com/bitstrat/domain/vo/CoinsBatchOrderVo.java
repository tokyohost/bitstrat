package com.bitstrat.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.bitstrat.domain.CoinsBatchOrder;
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
 * 分批任务订单记录视图对象 coins_batch_order
 *
 * @author Lion Li
 * @date 2025-04-26
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsBatchOrder.class)
public class CoinsBatchOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 批次id
     */
    @ExcelProperty(value = "批次id")
    private Long batchId;

    /**
     * 当前执行批次
     */
    @ExcelProperty(value = "当前执行批次")
    private Long currBatch;

    /**
     * 下单数量
     */
    @ExcelProperty(value = "下单数量")
    private Long orderSize;

    /**
     * 状态 10-已下单 20-已成交 30-异常
     */
    @ExcelProperty(value = "状态 10-已下单 20-已成交 30-异常")
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


}
