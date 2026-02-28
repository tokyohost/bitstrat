package com.bitstrat.domain.vo;

import com.bitstrat.domain.CoinAiTaskRequest;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * AI 用户请求提示词视图对象 coin_test_ai_request
 *
 * @author Lion Li
 * @date 2025-11-01
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinAiTaskRequest.class)
public class CoinAiTaskRequestVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 请求key
     */
    @ExcelProperty(value = "请求key")
    private String requestKey;

    /**
     * userPrompt
     */
    @ExcelProperty(value = "userPrompt")
    private String content;
    private String result;

    private Date responseTime;

    private Long token;

    private BigDecimal price;

    /**
     * 1-等待响应 2-已完成 3-已超时
     */
    private Long status;
    private String sysContent;
    private Long aiId;
    private CoinsAiConfigShow aiConfig;

    private Long taskId;
    private String errorMsg;

    private Date createTime;
    private Date updateTime;
}
