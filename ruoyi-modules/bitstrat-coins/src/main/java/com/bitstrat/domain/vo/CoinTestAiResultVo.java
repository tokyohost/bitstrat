package com.bitstrat.domain.vo;

import com.bitstrat.domain.CoinTestAiResult;
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
 * AI 操作日志视图对象 coin_test_ai_result
 *
 * @author Lion Li
 * @date 2025-10-30
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinTestAiResult.class)
public class CoinTestAiResultVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 操作
     */
    @ExcelProperty(value = "操作")
    private String action;

    /**
     * 杠杆
     */
    @ExcelProperty(value = "杠杆")
    private Long leverage;

    /**
     * 数量
     */
    @ExcelProperty(value = "数量")
    private String size;

    /**
     * 币对
     */
    @ExcelProperty(value = "币对")
    private String symbol;

    /**
     * 止盈
     */
    @ExcelProperty(value = "止盈")
    private String takeProfit;

    /**
     * 止损
     */
    @ExcelProperty(value = "止损")
    private String stopLoss;

    /**
     * 分析EN
     */
    @ExcelProperty(value = "分析EN")
    private String reasoningEn;

    /**
     * 分析zh
     */
    @ExcelProperty(value = "分析zh")
    private String reasoningZh;

    private Date createTime;

    private String think;
    private String requestKey;

    /**
     * 任务ID
     */
    private Long taskId;


    private String result;
}
