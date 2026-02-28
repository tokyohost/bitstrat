package com.bitstrat.ai.domain.abOrder;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceDiff {

    BigDecimal diffPlusAMinB;
    BigDecimal diffMinAPlusB;

    BigDecimal priceA;
    BigDecimal priceB;
}
