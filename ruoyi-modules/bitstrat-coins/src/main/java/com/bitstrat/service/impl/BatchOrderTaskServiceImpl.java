package com.bitstrat.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.*;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.CoinContractInfomation;
import com.bitstrat.domain.vo.*;
import com.bitstrat.service.*;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.AccountUtils;
import com.bitstrat.utils.RatioOperationBuilder;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.SpringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class BatchOrderTaskServiceImpl implements BatchOrderTaskService {



    @Autowired
    ICoinsBatchService coinsBatchService;
    @Autowired
    ICoinsNotifyService iCoinsNotifyService;

    @Autowired
    ICoinsApiService coinsApiService;

    @Autowired
    ExchangeApiManager exchangeApiManager;

    @Autowired
        @Lazy
    ICoinsCrossExchangeArbitrageTaskService coinsCrossExchangeArbitrageTaskService;

    @Autowired
    ICoinsOrderService coinsOrderService;

    @Autowired
    ICoinsAbBotService coinsAbBotService;
    @Autowired
    @Lazy
    ICoinsCrossTaskLogService coinsCrossTaskLogService;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ExecutorService executorService = Executors.newWorkStealingPool(1);
    @Autowired
    RedissonClient redissonClient;

    @Override
    public void asyncCheckAndRunBatchOrderTask() {
        BatchOrderTaskServiceImpl proxy = SpringUtils.getBean(this.getClass());
        executorService.submit(proxy::checkAndRunBatchOrderTask);

    }

    @Override
//    @Transactional(rollbackFor = Exception.class)
    public synchronized void checkAndRunBatchOrderTask() {
        //检查running 的批量入场任务，存在，就判断对应任务的订单是否都是终结状态，如果是终结状态就继续执行下一批
        List<CoinsBatchVo> coinsBatchVos = coinsBatchService.selectRunningTask();
        for (CoinsBatchVo coinsBatchVo : coinsBatchVos) {
            RLock lock = redissonClient.getLock(LockConstant.ORDER_BATCH_KEY_LOCK + ":" + coinsBatchVo.getId());
            if (lock.tryLock()) {
                try {     // manipulate protected state
                    //拿到锁之后拿最新的
                    CoinsBatchVo coinsBatchItem = coinsBatchService.queryById(coinsBatchVo.getId());
                    Integer notEndOrderCount = coinsOrderService.queryOrderCountByBatchId(coinsBatchItem.getId(), OrderEndStatus.NOT_END);
                    if (notEndOrderCount > 0) {
                        //存在没有结束的订单，不继续下单
                        continue;
                    }
                    if (coinsBatchItem.getDoneBatch()  >= coinsBatchItem.getBatchTotal()) {
                        //已经处理完了
                        coinsCrossTaskLogService.saveLog(coinsBatchVo.getTaskId(), "批量订单 batchid "+coinsBatchItem.getId() +"已处理完毕");
                        continue;
                    }
                    if(BatchOrderTaskStatus.RUNNING != coinsBatchItem.getStatus()){
                        continue;
                    }

                    List<CoinsOrderVo> coinsOrderVos = coinsOrderService.queryBatchUnEndOrderCount(coinsBatchItem.getId(), coinsBatchItem.getDoneBatch()+1);
                    long count = coinsOrderVos.stream().filter(item -> item.getOrderEnd() == 0).count();
                    if (count == 0) {
                        if (coinsBatchItem.getDoneBatch() == 0 && coinsOrderVos.isEmpty()) {
                            //第一次
                            try {
                                this.processBatchNext(coinsBatchItem);
                            } catch (Exception e) {
                                e.printStackTrace();
                                coinsCrossTaskLogService.saveLog(coinsBatchVo.getTaskId(), "批量订单 batchid "+coinsBatchItem.getId() +" 报错 :"+e.getMessage());
                                coinsBatchService.updateStatusById(coinsBatchItem.getId(), BatchOrderTaskStatus.ERROR,e.getMessage());
                                iCoinsNotifyService.sendNotification(coinsBatchItem.getUserId(),"批量出入场 异常\n"+e.getMessage()+"\n币对:"+coinsBatchItem.getSymbol()+"\n 总批次:"+coinsBatchItem.getBatchTotal()+" \n已完成批次:"
                                    +(coinsBatchItem.getDoneBatch())+"\n买入总数量:"+coinsBatchItem.getBuyTotal()+"\n买入已下单:"+coinsBatchItem.getDoneBuySize()+"\n卖出总数量:"
                                    +coinsBatchItem.getSellTotal()+"\n卖出已下单:"+coinsBatchItem.getDoneSellSize()+"\n开始时间:"+simpleDateFormat.format(coinsBatchItem.getStartTime())+
                                    "\n结束时间"+simpleDateFormat.format(new Date())+"\n 注意：会存在一腿已成交的情况，请检查任务任务持仓");
                            }
                        }else{
                            //表示done batch 已经结束
                            //处理已完成
                            for (CoinsOrderVo coinsOrderVo : coinsOrderVos) {
                                if (coinsOrderVo.getOrderEnd() == 1) {
                                    //订单已完成
                                    BigDecimal orderSize = new BigDecimal(coinsOrderVo.getSize());
                                    ExchangeService exchangeService = exchangeApiManager.getExchangeService(coinsOrderVo.getEx());
                                    orderSize = exchangeService.calcShowSize(coinsOrderVo.getSymbol(), orderSize);
                                    coinsBatchItem.setDoneSize(coinsBatchItem.getDoneSize()== null ?orderSize :coinsBatchItem.getDoneSize().add(orderSize) );
                                    if (coinsOrderVo.getSide().equalsIgnoreCase(SideType.LONG)) {
                                        //做多
                                        coinsBatchItem.setDoneBuySize(coinsBatchItem.getDoneBuySize() == null ? orderSize : orderSize.add(coinsBatchItem.getDoneBuySize()));
                                    }else if(coinsOrderVo.getSide().equalsIgnoreCase(SideType.SHORT)) {
                                        //做空
                                        coinsBatchItem.setDoneSellSize(coinsBatchItem.getDoneSellSize() == null ? orderSize : orderSize.add(coinsBatchItem.getDoneSellSize()));
                                    }
                                }
                            }
                            coinsBatchService.updateDoneSizeById(coinsBatchItem.getId(),coinsBatchItem.getDoneSize()
                                ,coinsBatchItem.getDoneBuySize(),coinsBatchItem.getDoneSellSize());
                            coinsBatchService.increaseDoneBatch(coinsBatchItem.getId());

                            //继续下一批
                            if (coinsBatchItem.getDoneBatch() + 1 == coinsBatchItem.getBatchTotal()) {
                                //已经是最后一批了
                                coinsBatchService.updateStatusById(coinsBatchItem.getId(), BatchOrderTaskStatus.SUCCESS,null);
                                coinsCrossTaskLogService.saveLog(coinsBatchVo.getTaskId(), "批量订单 batchid "+coinsBatchItem.getId() +"已处理完毕");
                                iCoinsNotifyService.sendNotification(coinsBatchItem.getUserId(),"批量出入场 处理完毕"+"\n币对:"+coinsBatchItem.getSymbol()+"\n 总批次:"+coinsBatchItem.getBatchTotal()+" \n已完成批次:"
                                    +(coinsBatchItem.getDoneBatch() + 1)+"\n买入总数量:"+coinsBatchItem.getBuyTotal()+"\n买入已下单:"+coinsBatchItem.getDoneBuySize()+"\n卖出总数量:"
                                    +coinsBatchItem.getSellTotal()+"\n卖出已下单:"+coinsBatchItem.getDoneSellSize()
                                +"\n开始时间:"+simpleDateFormat.format(coinsBatchItem.getStartTime())+
                                    "\n结束时间"+simpleDateFormat.format(new Date()));

                                //判断是否是机器人下单
                                if (coinsBatchItem.getBotId() != null) {
                                    //机器人下单
                                    if (coinsBatchItem.getSide() == 1) {
                                        //加仓
                                        coinsAbBotService.updateStatusById(coinsBatchItem.getBotId(), AbBotStatusConstant.HOLD);

                                    }else if(coinsBatchItem.getSide() == 2) {
                                        //平仓
//                                        coinsAbBotService.updateStatusById(coinsBatchItem.getBotId(), AbBotStatusConstant.RUNNING);
                                        //平仓可能没有平完，需要检查activetask id 是否是已平仓
                                    }

                                }
                                continue;
                            }
                            try {
                                this.processBatchNext(coinsBatchItem);
                            } catch (Exception e) {
                                e.printStackTrace();
                                coinsCrossTaskLogService.saveLog(coinsBatchVo.getTaskId(), "批量订单 batchid "+coinsBatchItem.getId() +" 报错 :"+e.getMessage());
                                coinsBatchService.updateStatusById(coinsBatchItem.getId(), BatchOrderTaskStatus.ERROR,e.getMessage());
                                iCoinsNotifyService.sendNotification(coinsBatchItem.getUserId(),"批量出入场 异常\n"+e.getMessage()+"\n币对:"+coinsBatchItem.getSymbol()+"\n 总批次:"+coinsBatchItem.getBatchTotal()+" \n已完成批次:"
                                +coinsBatchItem.getDoneBatch()+"\n买入总数量:"+coinsBatchItem.getBuyTotal()+"\n买入已下单:"+coinsBatchItem.getDoneBuySize()+"\n卖出总数量:"
                                +coinsBatchItem.getSellTotal()+"\n卖出已下单:"+coinsBatchItem.getDoneSellSize()+"\n开始时间:"+simpleDateFormat.format(coinsBatchItem.getStartTime())+
                                    "\n结束时间"+simpleDateFormat.format(new Date())+"\n 注意：会存在一腿已成交的情况，请检查任务任务持仓");
                            }
                        }


                    }else{
                        //订单还没终结态
                        continue;
                    }
                } finally {
                    lock.unlock();
                }
            } else {   // perform alternative actions
                log.error("异步更新仓位获取锁失败...");
            }

        }


    }

    @Override
    public void processBatchNext(CoinsBatchVo coinsBatchItemOld) {
        //计算下一批
        CoinsBatchVo coinsBatchItem = coinsBatchService.queryById(coinsBatchItemOld.getId());
        CoinsCrossExchangeArbitrageTaskVo coinsCrossExchangeArbitrageTaskVo = coinsCrossExchangeArbitrageTaskService.queryById(coinsBatchItem.getTaskId());
        Integer doneBatch = coinsBatchItem.getDoneBatch();
        int nextBatch = doneBatch + 1;
        String buyEx = coinsBatchItem.getBuyEx();
        String sellEx = coinsBatchItem.getSellEx();
        BigDecimal buyTotal = coinsBatchItem.getBuyTotal();
        BigDecimal sellTotal = coinsBatchItem.getSellTotal();
        BigDecimal currBuySize = BigDecimal.ZERO;
        BigDecimal currSellSize = BigDecimal.ZERO;

        CoinsApiVo longAccount = coinsApiService.queryById(coinsCrossExchangeArbitrageTaskVo.getLongAccountId());
        CoinsApiVo shortAccount = coinsApiService.queryById(coinsCrossExchangeArbitrageTaskVo.getShortAccountId());
        Account toAccountLong = AccountUtils.coverToAccount(longAccount);
        Account toAccountShort = AccountUtils.coverToAccount(shortAccount);
        List<Double> operations = RatioOperationBuilder.calculateOperations(coinsBatchItem.getBatchSize().doubleValue());
        if (nextBatch == coinsBatchItem.getBatchTotal().intValue()) {
            //最后一次下单所有
            currBuySize = buyTotal.subtract(coinsBatchItem.getDoneBuySize());
            currSellSize = sellTotal.subtract(coinsBatchItem.getDoneSellSize());
        }else{
            ExchangeService buyExchange = exchangeApiManager.getExchangeService(buyEx);
            ExchangeService sellExchange = exchangeApiManager.getExchangeService(sellEx);
            CoinContractInfomation buyContractCoinInfo = buyExchange.getContractCoinInfo(toAccountLong, coinsBatchItem.getSymbol());
            Double currSizePercent = operations.get(nextBatch);
            currBuySize = buyTotal.multiply(BigDecimal.valueOf(currSizePercent).divide(BigDecimal.valueOf(100),buyContractCoinInfo.getMinSz().scale()+2,RoundingMode.HALF_DOWN));
//                ,buyContractCoinInfo.getMinSz().scale(), RoundingMode.HALF_DOWN);
            CoinContractInfomation sellContractCoinInfo = sellExchange.getContractCoinInfo(toAccountShort, coinsBatchItem.getSymbol());
//            currSellSize = sellTotal.divide(BigDecimal.valueOf(currSizePercent), sellContractCoinInfo.getMinSz().scale(), RoundingMode.HALF_DOWN);
            currSellSize = sellTotal.multiply(BigDecimal.valueOf(currSizePercent).divide(BigDecimal.valueOf(100), sellContractCoinInfo.getMinSz().scale() + 2, RoundingMode.HALF_DOWN));
        }
        if(currSellSize.doubleValue() > 0d && currBuySize.doubleValue() > 0d){
            if (coinsBatchItem.getSide() == 1) {
                //加仓
                ArbitrageFormData buySide = new ArbitrageFormData();
                buySide.setOrderType(coinsBatchItem.getBuyOrderType());
                buySide.setSize(currBuySize);
                buySide.setLeverage(coinsBatchItem.getBuyLeverage());
                buySide.setAccountId(toAccountLong.getId());

                ArbitrageFormData sellSide = new ArbitrageFormData();
                sellSide.setOrderType(coinsBatchItem.getSellOrderType());
                sellSide.setSize(currSellSize);
                sellSide.setLeverage(coinsBatchItem.getSellLeverage());
                sellSide.setAccountId(toAccountShort.getId());

                AbTaskFrom abTaskFrom = new AbTaskFrom();
                abTaskFrom.setBuy(buySide);
                abTaskFrom.setSell(sellSide);
                abTaskFrom.setBatchId(coinsBatchItem.getId());
                abTaskFrom.setBatchFlag(true);
                abTaskFrom.setBatchCount(nextBatch);
                abTaskFrom.setTaskId(coinsBatchItem.getTaskId());
                log.info("自动下单- 加仓AbTaskFrom = {}", JSONObject.toJSONString(abTaskFrom));
                coinsCrossExchangeArbitrageTaskService.oncePlace2ExOrder(abTaskFrom);
            }
            else if(coinsBatchItem.getSide() == 2){
                //平仓
                ArbitrageFormData buySide = new ArbitrageFormData();
                buySide.setOrderType(coinsBatchItem.getBuyOrderType());
                buySide.setSize(currBuySize);
                buySide.setLeverage(coinsBatchItem.getBuyLeverage());

                ArbitrageFormData sellSide = new ArbitrageFormData();
                sellSide.setOrderType(coinsBatchItem.getSellOrderType());
                sellSide.setSize(currSellSize);
                sellSide.setLeverage(coinsBatchItem.getSellLeverage());

                AbTaskFrom abTaskFrom = new AbTaskFrom();
                abTaskFrom.setBuy(buySide);
                abTaskFrom.setSell(sellSide);
                abTaskFrom.setBatchId(coinsBatchItem.getId());
                abTaskFrom.setBatchCount(nextBatch);
                abTaskFrom.setBatchFlag(true);
                abTaskFrom.setTaskId(coinsBatchItem.getTaskId());
                log.info("自动下单- 平仓AbTaskFrom = {}", JSONObject.toJSONString(abTaskFrom));
                coinsCrossExchangeArbitrageTaskService.oncePlace2ExOrderClosePosition(abTaskFrom);
            }
        }else{
            log.error("计算下单数量小于等于0 异常！batchId= {}",coinsBatchItem.getId());
            coinsCrossTaskLogService.saveLog(coinsBatchItem.getTaskId(), "计算下单数量小于等于0 异常！ batchid "+coinsBatchItem.getId() +"已处理完毕");
        }







    }

    public static void main(String[] args) {
        BigDecimal multiply = BigDecimal.valueOf(200).multiply(BigDecimal.valueOf(52.2).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_DOWN));
        System.out.printf("multiply = %s",multiply);
    }
}
