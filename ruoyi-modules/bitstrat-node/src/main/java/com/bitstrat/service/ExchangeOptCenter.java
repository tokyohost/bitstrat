package com.bitstrat.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 11:24
 * @Content
 */

@Component
public class ExchangeOptCenter {
    HashMap<String, ExchangeService> exchangeServiceHashMap = new HashMap<>();
    public ExchangeOptCenter(List<ExchangeService> exchangeServices) {
        for (ExchangeService exchangeService : exchangeServices) {
            exchangeServiceHashMap.put(exchangeService.getExchangeName(), exchangeService);
        }

    }

    public ExchangeService getExchangeService(String exchangeName) {
        return exchangeServiceHashMap.get(exchangeName);
    }
}
