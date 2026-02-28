package com.bitstrat.domain.binance;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Filter {
    private String minPrice;
    private String maxPrice;
    private String filterType;
    private BigDecimal tickSize;
    private BigDecimal stepSize;
    private BigDecimal maxQty;
    private BigDecimal minQty;
    private Long limit;
    private BigDecimal notional;
    private BigDecimal multiplierDown;
    private BigDecimal multiplierUp;
    private Long multiplierDecimal;
}
