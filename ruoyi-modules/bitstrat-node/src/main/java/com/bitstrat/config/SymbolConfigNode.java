package com.bitstrat.config;

import lombok.Data;

@Data
public class SymbolConfigNode {
    /**
     *     symbol: BTCUSDT
     *     price: 0.2
     *     drawdown-limit: 10
     */
    private String symbol;
    private Double price;
    private Double drawdownLimit;
}
