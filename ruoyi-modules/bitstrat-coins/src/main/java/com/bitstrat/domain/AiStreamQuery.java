package com.bitstrat.domain;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/8 19:59
 * @Content
 */

@Data
public class AiStreamQuery {

    @NotEmpty(message = "请选择交易所")
    String exchange;
    @NotEmpty(message = "请选择币对")
    String symbol;
    @NotNull(message = "请选择API 账户")
    Long accountId;
    @NotNull(message = "请选择AI智能体")
    Long apiId;
    String content;
    String positionFlag;
    @NotEmpty(message = "请选择短期行情粒度")
    String shortTermInterval;
    @NotEmpty(message = "请选择长期行情粒度")
    String longTermInterval;
}
