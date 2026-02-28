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
public class QrPayCallBack {
    //    private CoinsBalanceLogBo coinsBalanceLogBo;
    private String tradeStatus;
    private String tradeNo;
    private String errorMsg;

    private Boolean isSuccess;

}
