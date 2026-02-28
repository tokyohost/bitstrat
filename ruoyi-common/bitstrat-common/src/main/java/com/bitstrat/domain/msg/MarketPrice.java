package com.bitstrat.domain.msg;

import com.bitstrat.domain.server.MessageData;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 17:58
 * @Content
 */


public class MarketPrice implements MessageData {
    String lastPrice;
    String symbol;

    public String getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(String lastPrice) {
        this.lastPrice = lastPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
