package com.bitstrat.task;

import com.aizuda.snailjob.client.job.core.MapHandler;
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.annotation.MapExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.job.core.dto.MapArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.alibaba.fastjson2.JSON;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.AccountBalance;
import com.bitstrat.domain.HistoryPosition;
import com.bitstrat.domain.HistoryPositionQuery;
import com.bitstrat.domain.bo.CoinAITaskBalanceBo;
import com.bitstrat.domain.bo.CoinApiPositionBo;
import com.bitstrat.domain.bo.CoinsAiTaskBo;
import com.bitstrat.domain.vo.CoinsAiTaskVo;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.service.*;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.AccountUtils;
import com.bitstrat.wsClients.WsClusterManager;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 定时统计AI任务的账户历史仓位情况
 */
@Component
@Slf4j
@JobExecutor(name = "aiTaskPositionSummaryTask")
public class AiTaskPositionSummaryTask {

    @Autowired
    ICoinsAiTaskService coinsAiTaskService;
    @Autowired
    ICoinApiPositionService coinApiPositionService;

    @Autowired
    ICoinsApiService coinsApiService;
    @Autowired
    WsClusterManager clusterManager;
    @Autowired
    ICoinAITaskBalanceService coinAITaskBalanceService;

    @Autowired
    ExchangeApiManager exchangeApiManager;

    private final Integer BATCH_SIZE = 2;

    public ExecuteResult jobExecute(JobArgs jobArgs) {
        SnailJobLog.LOCAL.info("开始定时统计AI任务历史持仓");
        long startTimeStamp = System.currentTimeMillis();
        this.run(null);
        SnailJobLog.LOCAL.info("定时统计AI任务历史持仓 耗时 {}ms", System.currentTimeMillis() - startTimeStamp);
        return ExecuteResult.success("定时统计AI任务历史持仓成功");
    }


    public void run(List<CoinsAiTaskVo> coinsAiTaskVos) {
        if(Objects.isNull(coinsAiTaskVos)){
            CoinsAiTaskBo coinsAiTaskBo = new CoinsAiTaskBo();
            coinsAiTaskBo.setStatus(2L);
            coinsAiTaskVos = coinsAiTaskService.queryList(coinsAiTaskBo);
        }
        //都是正在运行的任务，统计历史持仓
        for (CoinsAiTaskVo coinsAiTaskVo : coinsAiTaskVos) {
            ExchangeType exchangeType = ExchangeType.getExchangeType(coinsAiTaskVo.getExchange());
            Long apiId = coinsAiTaskVo.getApiId();
            CoinsApiVo coinsApiVo = coinsApiService.queryById(apiId);
            Account account = AccountUtils.coverToAccount(coinsApiVo);
            if (Objects.nonNull(exchangeType)) {
                ExchangeService exchangeService = exchangeApiManager.getExchangeService(exchangeType.getName());
                //查最新的100条
                List<HistoryPosition> historyPositions = exchangeService.queryContractHistoryPosition(account, 100L, new HistoryPositionQuery());
                HashMap<String, CoinApiPositionBo> boHashMap = new HashMap<>(historyPositions.size());

                for (HistoryPosition historyPosition : historyPositions) {
                    String positionId = exchangeService.getPositionId(historyPosition);
                    if(Objects.isNull(positionId)){
                        continue;
                    }
                    CoinApiPositionBo coinApiPositionBo = CoinAITaskBalanceBo.cover(historyPosition);
                    coinApiPositionBo.setApiId(apiId);
                    coinApiPositionBo.setUserId(coinsApiVo.getUserId());
                    coinApiPositionBo.setPositionId(positionId);
                    boHashMap.put(positionId, coinApiPositionBo);
                }

                //查用户下所有的仓位ID
                List<String> posIds = boHashMap.values().stream().map(CoinApiPositionBo::getPositionId).distinct().collect(Collectors.toList());
                List<String> dbExistsPosition = coinApiPositionService.selectIdsByApiIdAndCurrentId(posIds, apiId);
                Set<String> dbExists = dbExistsPosition.stream().collect(Collectors.toSet());
                boHashMap.values().removeIf(item -> dbExists.contains(item.getPositionId()));

                //新创建
                for (CoinApiPositionBo positionBo : boHashMap.values()) {
                    coinApiPositionService.insertByBo(positionBo);
                }
            }
        }
    }


    @MapExecutor
    public ExecuteResult rootMapExecute(MapArgs mapArgs, MapHandler mapHandler) {
        SnailJobLog.LOCAL.info("定时统计AI任务历史持仓任务开始切片 节点ID:{}", clusterManager.getNodeId());
        // 此处可以进行自定义的分片逻辑

        CoinsAiTaskBo coinsAiTaskBo = new CoinsAiTaskBo();
        coinsAiTaskBo.setStatus(2L);
        List<CoinsAiTaskVo> coinsAiTaskVos = coinsAiTaskService.queryList(coinsAiTaskBo);
        Object jobParams = mapArgs.getJobParams();
        String paramsSize = String.valueOf(jobParams);
        Integer bsize = BATCH_SIZE;
        if(NumberUtils.isParsable(paramsSize)){
            bsize = Integer.valueOf(paramsSize);
        }
        SnailJobLog.LOCAL.info("定时统计AI任务历史持仓任务切片 SIZE={} 节点ID:{}",bsize, clusterManager.getNodeId());
        List<Long> collect = coinsAiTaskVos.stream().map(CoinsAiTaskVo::getId).collect(Collectors.toList());
        List<List<Long>> partition = Lists.partition(collect, bsize);
        if(partition.size() == 0){
            return ExecuteResult.success("执行结束,暂无待执行任务");
        }
        // 服务端会获取到客户端的节点数量来处理map的各个分片
        // 根据在页面配置的路由策略，把对应的分片分发到不同客户端节点
        // 例如：入参总量为10条，BATCH_SIZE设置为3则每3条一个分组，分组结果（List<T> taskList）为[{0,3}, {4, 6}, {7,9}, {10}]，此时就会分为四组
        // 集合中的各个对象都会作为MapArgs的mapResult参数传递到后续的各个Map任务中
        // 此处的nextTaskName即为后续分发的Map任务名称
        ExecuteResult secondMap = mapHandler.doMap(partition, "aiTaskPositionSummaryTaskSecondMap");
        SnailJobLog.LOCAL.info("定时统计AI任务历史持仓任务切片结束 节点ID:{}", clusterManager.getNodeId());
        return secondMap;
    }

    // 由ROOT_MAP分片分发到SECOND_MAP任务
    @MapExecutor(taskName = "aiTaskPositionSummaryTaskSecondMap")
    public ExecuteResult secondMapExecute(MapArgs mapArgs) {
        SnailJobLog.REMOTE.info("定时统计AI任务历史持仓任务执行切片 {} 节点ID {}", JSON.toJSONString(mapArgs.getMapResult()),clusterManager.getNodeId());
        // mapResult 即为上述分包中分发下来的具体参数
        // 各节点分别处理分片后的业务逻辑
        // 测试目的2时取消注释
        //List<String> strings = JSON.parseArray(JSON.toJSONString(mapArgs.getMapResult()), String.class);
        //if (strings.contains("5")){
        //    int i = 1/0;
        //}
        List<Long> ids = JSON.parseArray(JSON.toJSONString(mapArgs.getMapResult()), Long.class);
        List<CoinsAiTaskVo> coinsAiTaskVos = coinsAiTaskService.queryListByIds(ids);
        this.run(coinsAiTaskVos);
        SnailJobLog.LOCAL.info("历史持仓执行结束 共完成{} 个节点ID:{}",ids.size(), clusterManager.getNodeId());
        return ExecuteResult.success("执行完毕");
    }

}
