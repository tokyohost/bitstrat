package com.bitstrat.strategy;

import com.bitstrat.domain.CoinsFinancialFlowRecord;
import com.bitstrat.domain.bo.CoinsFinancialFlowRecordBo;
import com.bitstrat.service.ICoinsFinancialFlowRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CoinsFinancialFlowService {
    private final Map<String, CoinsFinancialFlowStrategy> strategies;
    private final ICoinsFinancialFlowRecordService coinsFinancialFlowRecordService;

    public CoinsFinancialFlowService(List<CoinsFinancialFlowStrategy> strategyList,
                                     ICoinsFinancialFlowRecordService coinsFinancialFlowRecordService) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(CoinsFinancialFlowStrategy::getExchangeName, strategy -> strategy));
        this.coinsFinancialFlowRecordService = coinsFinancialFlowRecordService;
    }

    public void fetchAllFinancialFlows() {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(2);
        for (CoinsFinancialFlowStrategy strategy : strategies.values()) {
            try {
                List<CoinsFinancialFlowRecord> financialFlowRecords = strategy.getFinancialFlows(startTime, endTime);
                List<CoinsFinancialFlowRecordBo> coinsFinancialFlowRecordBos = financialFlowRecords
                    .stream().map(CoinsFinancialFlowRecord::toBo).collect(Collectors.toList());
                coinsFinancialFlowRecordService.insertBatch(coinsFinancialFlowRecordBos);
            } catch (Exception e) {
                log.error("Error processing financial flows for exchange: " + strategy.getExchangeName(), e);
            }
        }
    }
}
