package com.bitstrat.service;

import com.bitstrat.domain.bitget.UmcblItem;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/7 15:02
 * @Content bitget rest api 辅助接口
 */
public interface IBitgetApiService {
    List<UmcblItem> queryContracts(String umcbl);
    public ExecutorService getBitgetExecutorService();
}
