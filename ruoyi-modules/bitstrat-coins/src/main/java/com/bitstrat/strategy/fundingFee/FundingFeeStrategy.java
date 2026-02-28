package com.bitstrat.strategy.fundingFee;

import com.bitstrat.domain.CoinsCrossExchangeArbitrageTask;
import com.bitstrat.domain.FundFeeTask;
import com.bitstrat.domain.TaskBalance;
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;

/**
 * @author caoyang
 * @date 2025-05-02
 */
public interface FundingFeeStrategy {
    boolean isFundingFeeInverted(CoinsCrossExchangeArbitrageTaskVo task);

    FundFeeTask queryFundingFeeByTask(CoinsCrossExchangeArbitrageTaskVo task);

    TaskBalance queryBalanceByTask(CoinsCrossExchangeArbitrageTaskVo task);
}
