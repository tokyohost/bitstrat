package com.bitstrat.strategy;

import com.bitstrat.domain.CoinsFinancialFlowRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author caoyang
 * @date 2025-06-01
 */

public interface CoinsFinancialFlowStrategy {
    // 查询API某日的资金流水
    List<CoinsFinancialFlowRecord> getFinancialFlows(LocalDateTime startTime, LocalDateTime endTime);
    String getExchangeName();
}
