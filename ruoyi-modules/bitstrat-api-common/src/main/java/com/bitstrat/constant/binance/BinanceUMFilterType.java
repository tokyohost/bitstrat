package com.bitstrat.constant.binance;

public class BinanceUMFilterType {

    /**
     * 价格限制
     * {
     * "filterType": "PRICE_FILTER", // 价格限制
     * "maxPrice": "300", // 价格上限, 最大价格
     * "minPrice": "0.0001", // 价格下限, 最小价格
     * "tickSize": "0.0001" // 订单最小价格间隔
     * }
     */
    public static final String PRICE_FILTER = "PRICE_FILTER";
    /**
     * 数量限制
     * {
     * "filterType": "LOT_SIZE", // 数量限制
     * "maxQty": "10000000", // 数量上限, 最大数量
     * "minQty": "1", // 数量下限, 最小数量
     * "stepSize": "1" // 订单最小数量间隔
     * }
     */
    public static final String LOT_SIZE = "LOT_SIZE";
    /**
     * 市价订单数量限制
     * {
     * "filterType": "MARKET_LOT_SIZE", // 市价订单数量限制
     * "maxQty": "590119", // 数量上限, 最大数量
     * "minQty": "1", // 数量下限, 最小数量
     * "stepSize": "1" // 允许的步进值
     * }
     */
    public static final String MARKET_LOT_SIZE = "MARKET_LOT_SIZE";
    /**
     * 最多订单数限制
     * {
     * "filterType": "MAX_NUM_ORDERS", // 最多订单数限制
     * "limit": 200
     * }
     */
    public static final String MAX_NUM_ORDERS = "MAX_NUM_ORDERS";
    /**
     * 最多条件订单数限制
     * {
     * "filterType": "MAX_NUM_ALGO_ORDERS",
     * "limit": 10
     * }
     */
    public static final String MAX_NUM_ALGO_ORDERS = "MAX_NUM_ALGO_ORDERS";
    /**
     * 最小名义价值  （除开杠杆之后的名义价值）
     * {
     * "filterType": "MIN_NOTIONAL",
     * "notional": "5.0",
     * }
     */
    public static final String MIN_NOTIONAL = "MIN_NOTIONAL";
    /**
     * 价格比限制
     * {
     * "filterType": "PERCENT_PRICE", // 价格比限制
     * "multiplierUp": "1.1500", // 价格上限百分比
     * "multiplierDown": "0.8500", // 价格下限百分比
     * "multiplierDecimal": 4
     * }
     */
    public static final String PERCENT_PRICE = "PERCENT_PRICE";
}
