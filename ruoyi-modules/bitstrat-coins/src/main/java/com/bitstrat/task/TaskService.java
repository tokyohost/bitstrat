package com.bitstrat.task;


import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;

import java.util.List;

public interface TaskService {

    void startWsSocket(List<CoinsCrossExchangeArbitrageTaskVo> coinsCrossExchangeArbitrageTaskVos);
}
