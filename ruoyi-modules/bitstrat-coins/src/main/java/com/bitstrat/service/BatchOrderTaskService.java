package com.bitstrat.service;

import com.bitstrat.domain.vo.CoinsBatchVo;

public interface BatchOrderTaskService {


    /**
     * 异步检查并执行任务
     */
    public void asyncCheckAndRunBatchOrderTask();
    /**
     * 检查并执行任务
     */
    public void checkAndRunBatchOrderTask();

    void processBatchNext(CoinsBatchVo coinsBatchItem);
}
