package com.bitstrat.domain.bo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/4 9:14
 * @Content
 */

@Data
public class QrPayQueryParams {

    @NotNull(message = "订单号不能为空")
    private Long outTradeNo;
}
