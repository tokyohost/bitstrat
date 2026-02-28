package com.bitstrat.domain.msg;

import com.bitstrat.domain.server.ByBitMessageData;
import com.bitstrat.domain.server.MessageData;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 9:14
 * @Content 下单
 */

public class BybitOrder extends ByBitMessageData {
    BigDecimal price;
    BigDecimal quantity;
    String timeInForce;
    String type;
    String symbol;
    String category;

    /**
     * 止盈价格
     */
    String takeProfit;


    /**
     * 止损价格
     */
    String stopLoss;

    /**
     * 自定义订单id
     */
    String orderLinkId;

    public String getOrderLinkId() {
        return orderLinkId;
    }

    public void setOrderLinkId(String orderLinkId) {
        this.orderLinkId = orderLinkId;
    }

    public String getTakeProfit() {
        return takeProfit;
    }

    public void setTakeProfit(String takeProfit) {
        this.takeProfit = takeProfit;
    }

    public String getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(String stopLoss) {
        this.stopLoss = stopLoss;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

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
