package com.bitstrat.service.impl;

import com.bitstrat.domain.CoinsCrossExchangeArbitrageTask;
import com.bitstrat.domain.FundFeeTask;
import com.bitstrat.domain.TaskBalance;
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;
import com.bitstrat.strategy.fundingFee.FundingFeeStrategy;
import org.springframework.stereotype.Service;

/**
 * 获取资金费率是否倒挂等的服务实现类
 * @author caoyang
 * @date 2025-05-02
 */
@Service
public class FundingFeeService {
    private FundingFeeStrategy strategy;

    public FundingFeeService(FundingFeeStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean checkCrossExchangFundingFeeInversion(CoinsCrossExchangeArbitrageTaskVo task) {
        return strategy.isFundingFeeInverted(task);
    }

    public FundFeeTask queryFundingFeeByTask(CoinsCrossExchangeArbitrageTaskVo task) {
        return strategy.queryFundingFeeByTask(task);
    }

    public TaskBalance queryBalanceByTask(CoinsCrossExchangeArbitrageTaskVo task) {
        return strategy.queryBalanceByTask(task);
    }
}
