package com.bitstrat.domain.bitget;

import com.bitstrat.domain.TpSlOrder;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/10/27 17:32
 * @Content https://www.okx.com/docs-v5/zh/#order-book-trading-algo-trading-post-amend-algo-order
 */

@Data
public class UpdateTpSlOkx extends TpSlOrder {
    private String algoId;
    private String symbol; //交易币对
    private String newTpTriggerPx; //止盈触发价 如果止盈触发价或者委托价为0，那代表删除止盈
    private String newTpOrdPx; //止盈委托价 委托价格为-1时，执行市价止盈
    private String newSlTriggerPx; //止损触发价 委托价为0，那代表删除止损
    private String newSlOrdPx; //止损委托价 委托价格为-1时，执行市价止损

    /**
     * 止盈触发价类型
     * last：最新价格
     * index：指数价格
     * mark：标记价格
     */
    private String newTpTriggerPxType;

    /**
     * 止损触发价类型
     * last：最新价格
     * index：指数价格
     * mark：标记价格
     */
    private String newSlTriggerPxType;




}
