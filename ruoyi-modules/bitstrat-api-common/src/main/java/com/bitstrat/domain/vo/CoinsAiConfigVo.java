package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsAiConfig;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;


/**
 * ai 流水线配置视图对象 coins_ai_config
 *
 * @author Lion Li
 * @date 2025-04-01
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsAiConfig.class)
public class CoinsAiConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 流水线名称
     */
    @ExcelProperty(value = "流水线名称")
    private String flowName;

    /**
     * api地址
     */
    @ExcelProperty(value = "api地址")
    private String url;

    /**
     * token
     */
    @ExcelProperty(value = "token")
    private String token;


    private String imgUrl;

    private BigDecimal price;
    /**
     * 回调地址
     */
    private String callback;

    /**
     * 时间粒度
     */
    private String interval;

    private String type;
}
