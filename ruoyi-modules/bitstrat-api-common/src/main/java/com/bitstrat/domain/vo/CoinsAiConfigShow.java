package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsAiConfig;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/11/25 11:53
 * @Content
 */

@Data
@AutoMapper(target = CoinsAiConfigVo.class)
public class CoinsAiConfigShow {

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


    private String imgUrl;

    private BigDecimal price;
}
