package com.bitstrat.domain.bitget;

import com.bitstrat.domain.TpSlOrder;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/10/27 17:32
 * @Content https://www.bitget.com/zh-CN/api-doc/contract/plan/Modify-Tpsl-Order
 */

@Data
public class UpdateTpSl extends TpSlOrder {
    private String orderId;
    private String marginCoin; //	保证金币种
    private String productType; //产品类型
    private String symbol; //交易币对
    private String triggerType; //触发类型（fill_price （成交价格） mark_price（标记价格）
    private String executePrice; //执行价格 （若为0或不填则代表市价执行。若填写大于0，为限价执行。当planType（止盈止损类型）为moving_plan（移动止盈止损）时则不填，固定为市价执行。）
    private String triggerPrice; //触发价格
    private String size; //下单数量 对于仓位止盈止损订单，size必须为空"size":""





}
