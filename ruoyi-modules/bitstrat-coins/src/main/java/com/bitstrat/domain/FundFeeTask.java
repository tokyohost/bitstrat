package com.bitstrat.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/7 19:07
 * @Content
 */

@Data
public class FundFeeTask {
    BigDecimal longFundingFee;
    BigDecimal shortFundingFee;
    LocalDateTime longNextFundingTime;
    LocalDateTime shortNextFundingTime;
    BigDecimal fundingFeeCanMake;
    BigDecimal apy;

    BigDecimal longMarketPrice;
    BigDecimal shortMarketPrice;
}
