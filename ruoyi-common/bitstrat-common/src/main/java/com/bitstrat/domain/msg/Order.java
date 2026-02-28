package com.bitstrat.domain.msg;

import com.bitstrat.domain.server.MessageData;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 9:14
 * @Content 下单
 */

public class Order implements MessageData {
    BigDecimal price;
    BigDecimal quantity;
    String timeInForce;
    String type;
    String symbol;


    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getTimeInForce() {
        return timeInForce;
    }

    public void setTimeInForce(String timeInForce) {
        this.timeInForce = timeInForce;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
