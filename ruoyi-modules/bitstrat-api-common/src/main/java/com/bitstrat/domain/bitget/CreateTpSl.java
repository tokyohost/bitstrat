package com.bitstrat.domain.bitget;

import com.bitstrat.domain.TpSlOrder;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/10/27 17:49
 * @Content
 */

@Data
public class CreateTpSl extends TpSlOrder {
    private String marginCoin;
    private String productType;
    private String symbol;
    /***
     * 止盈止损类型
     * profit_plan：止盈计划
     * loss_plan：止损计划
     * moving_plan：移动止盈止损
     * pos_profit：仓位止盈
     * pos_loss：仓位止损
     */
    private String planType;
    private String triggerPrice;
    /**
     * 触发类型
     * fill_price：市场价格
     * mark_price：标记价格
     */
    private String triggerType;
    /**
     * 执行价格
     * 不传或者传0则市价执行。大于0为限价执行
     * planType为moving_plan时不允许传此参数，固定为市价执行
     */
    private String executePrice;

    /**
     * 双向持仓：
     * long：多仓，short：空仓;
     * 单向持仓：
     * buy：多仓，sell：空仓
     */
    private String holdSide;

    /**
     * 下单数量(基础币)
     * planType为profit_plan、loss_plan、moving_plan时必填（大于0）
     * planType为pos_profit、pos_loss时非必填
     */
    private String size;











}
