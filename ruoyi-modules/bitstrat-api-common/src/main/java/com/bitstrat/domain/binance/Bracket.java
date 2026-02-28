package com.bitstrat.domain.binance;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Bracket {
    private Double maintMarginRatio;
    private Long bracket;
    private Long notionalFloor;
    private Long cum;
    private Long notionalCap;
    private Integer initialLeverage;
}
