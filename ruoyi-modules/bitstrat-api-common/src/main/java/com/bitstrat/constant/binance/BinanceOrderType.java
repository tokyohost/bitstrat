package com.bitstrat.constant.binance;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/20 17:03
 * @Content
 *1. LIMIT（限价单）
 * 说明：你指定价格和数量，订单会挂在订单簿中等待成交。
 *
 * 参数要求：price 必填。
 *
 * 场景：你想以更好的价格成交，愿意等待。
 *
 * 2. MARKET（市价单）
 * 说明：系统以当前市场最优价格立即成交。
 *
 * 参数要求：不需要 price。
 *
 * 场景：你想立即买入或卖出。
 *
 * 3. STOP（止损限价单）
 * 说明：价格达到 stopPrice 后，触发一个限价单（使用 price）。
 *
 * 参数要求：stopPrice、price 都要设置。
 *
 * 场景：设定止损位，一旦市场走坏，就触发限价平仓。
 *
 * 4. TAKE_PROFIT（止盈限价单）
 * 类似 STOP，但用于止盈。
 *
 * 参数要求：stopPrice、price 都要设置。
 *
 * 5. STOP_MARKET（止损市价单）
 * 说明：价格达到 stopPrice 后，触发一个市价单。
 *
 * 参数要求：只需 stopPrice。
 *
 * 场景：更快止损，确保尽快出场。
 *
 * 6. TAKE_PROFIT_MARKET（止盈市价单）
 * 用于达到止盈点后立刻以市价成交。
 *
 * 7. TRAILING_STOP_MARKET（追踪止损市价单）
 * 说明：设置一个回撤比例（callbackRate），系统自动跟踪市场最高/最低价并设置止损点。
 *
 * 参数要求：callbackRate，可选 activationPrice。
 *
 * 场景：趋势行情中动态保护利润。
 *
 *
 */

public class BinanceOrderType {
    /**
     * 	限价单。以指定价格挂单买入或卖出。
     */
    public static final String LIMIT = "LIMIT";
    /**
     * 市价单。以市场当前最优价格立即成交。
     */
    public static final String MARKET = "MARKET";
    /**
     * 	触发价格后，以限价方式下单。即“止损限价单”。
     */
    public static final String STOP = "STOP";
    /**
     * 触发价格后，以限价方式下单。即“止盈限价单”。
     */
    public static final String TAKE_PROFIT = "TAKE_PROFIT";
    /**
     * 	触发价格后，以市价方式下单。即“止损市价单”。
     */
    public static final String STOP_MARKET = "STOP_MARKET";
    /**
     * 触发价格后，以市价方式下单。即“止盈市价单”。
     */
    public static final String TAKE_PROFIT_MARKET = "TAKE_PROFIT_MARKET";
    /**
     * 追踪止损单，随市场波动动态调整止损价格。
     */
    public static final String TRAILING_STOP_MARKET = "TRAILING_STOP_MARKET";


}
