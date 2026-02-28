package com.bitstrat.ai.distuptor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/16 15:18
 * @Content
 */
@Data
public class MarketPrice {
    private String exchange;
    private String symbol;
    //交易所时间
    private long timestamp;
    //系统时间
    private long serverTimestamp;
    private BigDecimal price;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private BigDecimal lastPrice;
    private BigDecimal indexPrice;
    private BigDecimal markPrice;
    private int side;

    @JsonIgnore
    private ABDisruptor abDisruptor;

    public MarketPrice(long timestamp, BigDecimal price) {
        this.timestamp = timestamp;
        this.price = price;
    }

    @Override
    public String toString() {
        return "MarketPrice{" + "timestamp=" + timestamp + ", price=" + price + '}';
    }
}
