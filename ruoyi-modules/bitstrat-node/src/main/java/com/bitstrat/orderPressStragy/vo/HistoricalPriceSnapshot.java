package com.bitstrat.orderPressStragy.vo;

public class HistoricalPriceSnapshot {
    private Double price30s = null;
    private long timestamp30s = 0;

    private Double price60s = null;
    private long timestamp60s = 0;

    /**
     * 每次有新价格数据就调用这个方法
     * @param currentPrice 当前价格
     * @param currentTimeMillis 当前时间戳（毫秒）
     */
    public void update(double currentPrice, long currentTimeMillis) {
        if (currentTimeMillis - timestamp60s >= 60_000) {
            price60s = currentPrice;
            timestamp60s = currentTimeMillis;
        }

        if (currentTimeMillis - timestamp30s >= 30_000) {
            price30s = currentPrice;
            timestamp30s = currentTimeMillis;
        }
    }

    public Double getPrice30s() {
        return price30s;
    }

    public Double getPrice60s() {
        return price60s;
    }

    public boolean isReady() {
        return price30s != null && price60s != null;
    }

    @Override
    public String toString() {
        return "30sPrice=" + price30s + ", 60sPrice=" + price60s;
    }
}
