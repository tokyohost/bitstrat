package com.bitstrat.task;

import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.bitstrat.config.ExchangeWebsocketProperties;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;
import com.bitstrat.service.ICoinsApiService;
import com.bitstrat.service.ICoinsCrossExchangeArbitrageTaskService;
import com.bitstrat.service.ICoinsOrderService;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.wsClients.ExchangeConnectionManager;
import com.bitstrat.wsClients.constant.WebSocketType;
import com.bitstrat.wsClients.msg.SubscriptMsgs;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/28 16:15
 * @Content 检查活跃的持仓，并创建websocket 连接监听订单和仓位
 */
@Component
@Slf4j
@JobExecutor(name = "crossPositionWebsocketTask")
public class CrossPositionWebsocketTask  implements InitializingBean {

    @Autowired
    ICoinsCrossExchangeArbitrageTaskService coinsCrossExchangeArbitrageTaskService;

    @Autowired
    ICoinsOrderService coinsOrderService;

    @Autowired
    ExchangeWebsocketProperties exchangeWebsocketProperties;

    @Autowired
    ExchangeConnectionManager exchangeConnectionManager;

    @Autowired
    ExchangeApiManager exchangeApiManager;

    @Autowired
    ICoinsApiService coinsApiService;

    @Autowired
    TaskService taskService;

    public CrossPositionWebsocketTask() {

    }

    public ExecuteResult jobExecute(JobArgs jobArgs) {
        SnailJobLog.LOCAL.info("开始检查持仓Ws情况");
        long startTimeStamp = System.currentTimeMillis();
        this.run();
        SnailJobLog.LOCAL.info("结束检查持仓Ws情况 耗时 {}ms", System.currentTimeMillis() - startTimeStamp);
        return ExecuteResult.success("检查持仓Ws情况成功");
    }


    public void run() {
        //先查出没有平仓的持仓任务
        List<CoinsCrossExchangeArbitrageTaskVo> coinsCrossExchangeArbitrageTaskVos = coinsCrossExchangeArbitrageTaskService.queryHandleList();
//        taskService.startWsSocket(coinsCrossExchangeArbitrageTaskVos);

    }


    @Override
    public void afterPropertiesSet() throws Exception {
        this.run();
    }
}
