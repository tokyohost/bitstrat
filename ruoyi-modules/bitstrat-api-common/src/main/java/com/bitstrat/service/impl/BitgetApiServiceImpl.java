package com.bitstrat.service.impl;

import com.bitget.openapi.common.client.BitgetRestClient;
import com.bitstrat.config.BitgetClientService;
import com.bitstrat.domain.bitget.UmcblItem;
import com.bitstrat.service.IBitgetApiService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/7 15:03
 * @Content
 */

@Service
public class BitgetApiServiceImpl implements IBitgetApiService {
    ExecutorService bitgetExecutorService = Executors.newWorkStealingPool(4);

    public ExecutorService getBitgetExecutorService() {
        return bitgetExecutorService;
    }

    @Override
    public List<UmcblItem> queryContracts(String umcbl) {


        return List.of();
    }
}
