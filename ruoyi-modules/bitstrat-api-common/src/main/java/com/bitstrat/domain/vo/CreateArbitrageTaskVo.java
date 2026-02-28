package com.bitstrat.domain.vo;

import com.bitstrat.domain.bo.CoinsCrossExchangeArbitrageTaskBo;
import com.bitstrat.domain.coinGlass.CoinFundingInfo;
import lombok.Data;

/**
 * 创建跨交易所套利任务
 */
@Data
public class CreateArbitrageTaskVo {
    AbTaskFrom from;
    CoinFundingInfo argitrageData;

    /**
     * 是否分批检查、平仓
     * 1- 分批建仓 0- 不分批
     */
    private Integer batchIncome;
    private Double batchSize;

    /**
     * 单腿下单，1-是 0-否
     */
    private Integer singleOrder;

    /**
     *单腿下单方向 buy / sell
     */
    private String singleOrderSide;


    private CoinsCrossExchangeArbitrageTaskBo taskBo;
    private Long botId;

}
