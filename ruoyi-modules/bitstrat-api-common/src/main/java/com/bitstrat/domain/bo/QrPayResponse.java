package com.bitstrat.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/3 19:29
 * @Content
 */

@Data
@AutoMapper(target = QrPayParams.class)
public class QrPayResponse {
//    private CoinsBalanceLogBo coinsBalanceLogBo;

    private String payType;

    private BigDecimal payAmount;

    private Long userId;

    private String qrCodeBase64;
    private String redirectUrl;

    private Long outTradeNo;

}
