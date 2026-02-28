package com.bitstrat.task;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/22 10:48
 * @Content
 */

import com.aizuda.snailjob.client.job.core.annotation.JobExecutor;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bitstrat.constant.CrossTaskStatus;
import com.bitstrat.constant.LockConstant;
import com.bitstrat.domain.CoinsCrossExchangeArbitrageTask;
import com.bitstrat.domain.bo.CoinsCrossExchangeArbitrageTaskBo;
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;
import com.bitstrat.domain.vo.CoinsOrderVo;
import com.bitstrat.service.ICoinsCrossExchangeArbitrageTaskService;
import com.bitstrat.service.ICoinsOrderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 跨交易所套利持仓定时同步
 */
@Component
@Slf4j
@JobExecutor(name = "crossPositionTask")
public class CrossPositionTask {
    @Autowired
    ICoinsCrossExchangeArbitrageTaskService coinsCrossExchangeArbitrageTaskService;

    @Autowired
    ICoinsOrderService coinsOrderService;

    @Autowired
    RedissonClient redissonClient;


    public ExecuteResult jobExecute(JobArgs jobArgs) {
        SnailJobLog.LOCAL.info("开始同步跨交易所套利持仓情况");
        long startTimeStamp = System.currentTimeMillis();
        this.run();
        SnailJobLog.LOCAL.info("结束同步跨交易所套利持仓 耗时 {}ms", System.currentTimeMillis() - startTimeStamp);
        return ExecuteResult.success("同步跨交易所套利持仓成功");
    }


//    @Scheduled(cron = "*/30 * * * * *")
    @Transactional(rollbackFor = Exception.class)
    public synchronized void run() {
        RLock lock = redissonClient.getLock(LockConstant.POSITION_SYNC_LOCK);
        lock.lock();
        try{
            //先查出没有平仓的持仓任务
            List<CoinsCrossExchangeArbitrageTaskVo> coinsCrossExchangeArbitrageTaskVos = coinsCrossExchangeArbitrageTaskService.queryHandleList();
            Map<Long, List<CoinsCrossExchangeArbitrageTaskVo>> userTaskGroup = coinsCrossExchangeArbitrageTaskVos.stream().collect(Collectors.groupingBy(CoinsCrossExchangeArbitrageTaskVo::getUserId));

            for (Long userId : userTaskGroup.keySet()) {
                List<CoinsCrossExchangeArbitrageTaskVo> groupByUser = userTaskGroup.getOrDefault(userId, new ArrayList<>());
                //开始查询
                List<CoinsCrossExchangeArbitrageTaskVo> taskVoList = coinsCrossExchangeArbitrageTaskService.syncPosition(groupByUser, userId);

                //如果两方持仓为0 或者null 判断为已平仓
                for (CoinsCrossExchangeArbitrageTaskVo position : taskVoList) {
                    boolean longClose = false;
                    if (Objects.isNull(position.getLongSymbolSize())
                        || position.getLongSymbolSize().doubleValue() == 0d) {
                        longClose = true;
                    }
                    boolean shortClose = false;
                    if (Objects.isNull(position.getShortSymbolSize())
                        || position.getShortSymbolSize().doubleValue() == 0d) {
                        shortClose = true;
                    }
                    if (longClose && shortClose) {
                        if (position.getStatus() == CrossTaskStatus.CREATED || position.getStatus() == CrossTaskStatus.WAIT_ORDER_DEAL) {
                            SnailJobLog.LOCAL.info("用户 {} 币对 {} 交易所{}-{} 仓位 {}-{} ，任务状态为刚创建/等待成交 不处理", position.getUserId(), position.getSymbol(),
                                position.getLongEx(), position.getShortEx(), position.getLongSymbolSize(), position.getShortSymbolSize());
                            continue;
                        }
                        //继续同步订单和仓位
                        syncClosePositionOrder(position);
                        SnailJobLog.LOCAL.info("用户 {} 币对 {} 交易所{}-{} 均已平仓，任务状态修改为已平仓", position.getUserId(), position.getSymbol(),
                            position.getLongEx(), position.getShortEx());
                        coinsCrossExchangeArbitrageTaskService.updateTaskStatus(position.getId(),CrossTaskStatus.CLOSED);
                    } else {
                        if (position.getStatus() == CrossTaskStatus.RUNNING) {
                            SnailJobLog.LOCAL.info("用户 {} 币对 {} 交易所{}-{} 仓位 {}-{} ，任务状态为正在运行不处理", position.getUserId(), position.getSymbol(),
                                position.getLongEx(), position.getShortEx(), position.getLongSymbolSize(), position.getShortSymbolSize());
                            continue;
                        } else if (position.getStatus() == CrossTaskStatus.CREATED || position.getStatus() == CrossTaskStatus.WAIT_ORDER_DEAL) {
                            SnailJobLog.LOCAL.info("用户 {} 币对 {} 交易所{}-{} 仓位 {}-{} ，任务状态为刚创建/等待成交 不处理", position.getUserId(), position.getSymbol(),
                                position.getLongEx(), position.getShortEx(), position.getLongSymbolSize(), position.getShortSymbolSize());
                            continue;
                        } else {
                            SnailJobLog.LOCAL.info("用户 {} 币对 {} 交易所{}-{} 仓位 {}-{} ，任务状态修改为正在运行", position.getUserId(), position.getSymbol(),
                                position.getLongEx(), position.getShortEx(), position.getLongSymbolSize(), position.getShortSymbolSize());

                            coinsCrossExchangeArbitrageTaskService.updateTaskStatus(position.getId(), CrossTaskStatus.RUNNING);
                        }

                    }

                }
            }
        }finally {
            lock.unlock();
        }

    }


    private void syncClosePositionOrder(CoinsCrossExchangeArbitrageTaskVo position) {
        List<CoinsOrderVo> userOrders = coinsOrderService.queryAllOrderByTaskIds(List.of(position.getId()));
        coinsOrderService.syncOrder(userOrders, position.getUserId(),false);
    }
}
