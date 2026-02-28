package com.bitstrat.service.impl;

import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.server.Message;
import com.bitstrat.service.ExchangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OkxExchangeServiceImpl implements ExchangeService {
    @Override
    public void buy(Message message) {

    }

    @Override
    public void amend(Message message) {

    }

    @Override
    public void sell(Message message) {

    }

    @Override
    public void cancel(Message message) {

    }

    @Override
    public String getExchangeName() {
        return ExchangeType.OKX.getName().toLowerCase();
    }

    @Override
    public void initSocket(Message account) {
        log.info("初始化okx");

    }
}
