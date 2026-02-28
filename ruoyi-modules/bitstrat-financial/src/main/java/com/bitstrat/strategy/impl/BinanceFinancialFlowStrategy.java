package com.bitstrat.strategy.impl;

import com.alibaba.fastjson2.JSONArray;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.bitstrat.domain.CoinsFinancialFlowRecord;
import com.bitstrat.domain.binance.IncomeItem;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.service.ICoinsApiService;
import com.bitstrat.service.ICoinsFinancialFlowRecordService;
import com.bitstrat.strategy.CoinsFinancialFlowStrategy;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BinanceFinancialFlowStrategy implements CoinsFinancialFlowStrategy {
    @Autowired
    private ICoinsFinancialFlowRecordService coinsFinancialFlowRecordService;
    @Autowired
     private ICoinsApiService coinsApiService;

    @Override
    public List<CoinsFinancialFlowRecord> getFinancialFlows(LocalDateTime startTime, LocalDateTime endTime) {
        String path = "fapi/v1/income";
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        ZoneId zoneId = ZoneId.systemDefault(); // 或 ZoneId.of("Asia/Shanghai")

        // 转为时间戳（毫秒）
        long startMillis = startTime.atZone(zoneId).toInstant().toEpochMilli();
        long endMillis = startTime.atZone(zoneId).toInstant().toEpochMilli();
        parameters.put("startTime", startMillis);
        parameters.put("endTime", endMillis);
        parameters.put("limit", 1000);
        // 查询当前用户的api

        Long userId = LoginHelper.getUserId();
        CoinsApiVo coinsApiVo = coinsApiService.queryApiByUserIdAndExchange(userId, getExchangeName());
        if (coinsApiVo != null && StringUtils.isNotEmpty(coinsApiVo.getApiKey()) &&  StringUtils.isNotEmpty(coinsApiVo.getApiSecurity())) {
            UMFuturesClientImpl client = new UMFuturesClientImpl(coinsApiVo.getApiKey(), coinsApiVo.getApiSecurity());
            String incomeHistory = client.account().getIncomeHistory(parameters);
            List<IncomeItem> incomeItems = JSONArray.parseArray(incomeHistory).toList(IncomeItem.class);
             return incomeItems.stream()
                .map(incomeItem -> {

//                    CoinsFinancialFlowRecord.builder()
//                            .exchangeRecordId(incomeItem.getTranId())
//                            .exchange("BINANCE")
//                            .flowType(incomeItem.getIncomeType())
//                            .symbol(incomeItem.getSymbol())
//                            .timestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(incomeItem.getTime()), ZoneOffset.UTC))
//                            .amount(incomeItem.getIncome()).build();

                    CoinsFinancialFlowRecord coinsFinancialFlowRecord = new CoinsFinancialFlowRecord();
                    coinsFinancialFlowRecord.setExchangeRecordId(incomeItem.getTranId());
                    coinsFinancialFlowRecord.setExchange("BINANCE");
                    coinsFinancialFlowRecord.setSymbol(incomeItem.getSymbol());
                    coinsFinancialFlowRecord.setFlowType(incomeItem.getIncomeType());
                    coinsFinancialFlowRecord.setTimestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(incomeItem.getTime()), ZoneOffset.UTC));
                    coinsFinancialFlowRecord.setAmount(incomeItem.getIncome());
                    return coinsFinancialFlowRecord;
                    }
                ). collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public String getExchangeName() {
        return "BINANCE";
    }
}
