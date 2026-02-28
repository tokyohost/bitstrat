package com.bitstrat.ai.distuptor;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/16 15:19
 * @Content
 */

public class MarketPriceEvent {
    private MarketPrice marketPrice;

    public int getSide() {
        return marketPrice.getSide();
    }

    public void setSide(int side) {
        this.marketPrice.setSide(side);
    }

    public void setMarketPrice(MarketPrice marketPrice) {
        this.marketPrice = marketPrice;
    }

    public MarketPrice getMarketPrice() {
        return marketPrice;
    }
}
