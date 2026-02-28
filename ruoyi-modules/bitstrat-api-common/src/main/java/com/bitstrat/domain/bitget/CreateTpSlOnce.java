package com.bitstrat.domain.bitget;

import com.bitstrat.domain.TpSlOrder;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/10/27 17:49
 * @Content https://www.bitget.com/zh-CN/api-doc/contract/plan/Place-Pos-Tpsl-Order
 */

@Data
public class CreateTpSlOnce extends TpSlOrder {
    private String marginCoin;
    private String productType;
    private String symbol;
    /**
     * 止盈触发价格
     */
    private String stopSurplusTriggerPrice;

    /**
     * 止盈数量，如果传，则是部分止盈计划，如果不传，则是仓位止盈
     */
    private String stopSurplusSize;

    /**
     * 触发类型
     * fill_price：市场价格
     * mark_price：标记价格
     */
    private String stopSurplusTriggerType;


    /**
     * 止盈执行价格
     * 不传或者传0则市价执行。大于0为限价执行
     */
    private String stopSurplusExecutePrice;

    /**
     * 	止损触发价格
     */
    private String stopLossTriggerPrice;

    /**
     * 止损数量，如果传，则是部分止损计划，如果不传，则是仓位止损
     */
    private String stopLossSize;

    /**
     * 触发类型
     * fill_price：市场价格
     * mark_price：标记价格
     */
    private String stopLossTriggerType;

    /**
     * 止损执行价格
     * 不传或者传0则市价执行。大于0为限价执行
     */
    private String stopLossExecutePrice;

    /**
     * 双向持仓：
     * long：多仓，short：空仓;
     * 单向持仓：
     * buy：多仓，sell：空仓
     */
    private String holdSide;

    private String side;
















}
