package com.bitstrat.ai.distuptor;

import com.bitstrat.ai.constant.ABOrderSideType;
import com.bitstrat.ai.domain.abOrder.ABOperate;
import com.bitstrat.ai.domain.abOrder.ABOrderTask;
import com.bitstrat.ai.domain.abOrder.PriceDiff;
import com.bitstrat.ai.handler.MarketABOrderStore;
import com.bitstrat.ai.utils.PriceDiffUtil;
import com.bitstrat.constant.OrderType;
import com.bitstrat.constant.SideType;
import com.bitstrat.domain.Event.AckSendAndSaveABOrderLogEvent;
import com.bitstrat.domain.PositionWsData;
import com.bitstrat.domain.vo.ABCloseFrom;
import com.bitstrat.domain.vo.ABOrderFrom;
import com.bitstrat.domain.vo.ArbitrageFormData;
import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.SpringUtils;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/16 15:19
 * @Content
 */

@Component
@Slf4j
public class MarketABPriceEventHandler implements EventHandler<MarketPriceEvent> {


    @Override
    public void onEvent(MarketPriceEvent event, long sequence, boolean endOfBatch) {
        ABDisruptor abDisruptor = event.getMarketPrice().getAbDisruptor();
        Long userId = abDisruptor.getUserId();
        log.info("收到并对比 Market Price Event: {}, side {} userId {}", event.getMarketPrice().toString()
            , event.getSide()
            , userId);
        PriceDiff priceDiff = null;
        try{
            if (event.getSide() == 1) {
                MarketPriceDisruptor disruptorB = abDisruptor.getDisruptorB();
                MarketPrice latestPrice = disruptorB.getLatestPrice();
                BigDecimal priceB = latestPrice.getPrice();
                BigDecimal priceA = event.getMarketPrice().getPrice();
                //计算价差
                priceDiff = calcPriceDiff(priceA, priceB);


            } else if (event.getSide() == 2) {
                MarketPriceDisruptor disruptorA = abDisruptor.getDisruptorA();
                MarketPrice latestPrice = disruptorA.getLatestPrice();
                BigDecimal priceA = latestPrice.getPrice();
                BigDecimal priceB = event.getMarketPrice().getPrice();
                //计算+A -B
                priceDiff = calcPriceDiff(priceA, priceB);
            }
        }catch (Exception e){
            log.error("calc price diff error: {}", e.getMessage());
            return;
        }


        if (Objects.nonNull(priceDiff)) {
            //计算完成
            log.info("symbol {} exA={} exB={} +A-B={} +B-A={}", event.getMarketPrice().getSymbol()
                , abDisruptor.disruptorA.getExchangeName()
                , abDisruptor.getDisruptorB().getExchangeName(),
                priceDiff.getDiffPlusAMinB() + "%"
                , priceDiff.getDiffMinAPlusB() + "%");


            //判断是否下单或平仓
            ABOrderTask abOrderTask = abDisruptor.getAbOrderTask();
            ABOperate operate = abOrderTask.getOperate();
            long currTime = System.currentTimeMillis();

            if (operate.getStatus().equalsIgnoreCase("stop")) {
                log.info("任务已停止，不处理");
                return;
            }
            ReentrantLock lock = abDisruptor.getLock();
            if (lock.tryLock()) {
                try {

                    orderCompare(abDisruptor, priceDiff);

                } finally {
                    lock.unlock();
                }
            } else {
                // 没获取到锁，直接放弃
                log.info(Thread.currentThread().getName() + " 放弃执行，锁已被占用");
            }


        }
    }

    /**
     * 下单、平仓核心逻辑
     *
     * @param abDisruptor
     * @param priceDiff
     */
    private void orderCompare(ABDisruptor abDisruptor, PriceDiff priceDiff) {
        ABOrderTask abOrderTask = abDisruptor.getAbOrderTask();
        ABOperate operate = abOrderTask.getOperate();
        String type = operate.getType();
        HashSet<PositionWsData> positionByAccountA = MarketABOrderStore.getPositionByAccountId(abOrderTask.getAccountA().getId());
        HashSet<PositionWsData> positionByAccountB = MarketABOrderStore.getPositionByAccountId(abOrderTask.getAccountB().getId());
        long currTime = System.currentTimeMillis();

        if (ABOrderSideType.PlusAminB.equalsIgnoreCase(type)) {
            //+A-B
            if("1".equalsIgnoreCase(operate.getReduceOnly())){
                //仅减仓 不触发下单

            }else{
                if (priceDiff.getDiffPlusAMinB().compareTo(operate.getOpenGap()) >= 0) {
                    //满足下单
                    if (Objects.nonNull(abOrderTask.getLastOrderTimeStamp())
                        && ((currTime - abOrderTask.getLastOrderTimeStamp()) / 1000) < operate.getColdSec()) {
                        log.info("在冷却时间内，暂停下单");
                    } else {
                        //判断持仓
                        boolean a = checkOrderPosition(positionByAccountA, operate, abOrderTask, "A", SideType.LONG);
                        if (a) {
                            boolean b = checkOrderPosition(positionByAccountB, operate, abOrderTask, "B", SideType.SHORT);
                            if (b) {
                                //可以下单
                                abOrderTask.setLastOrderTimeStamp(currTime);
                                AckSendAndSaveABOrderLogEvent ackSendAndSaveABOrderLogEvent = new AckSendAndSaveABOrderLogEvent();
                                ackSendAndSaveABOrderLogEvent.setOrderTask(abOrderTask);
                                ackSendAndSaveABOrderLogEvent.setMsg("满足 +A -B 下单 A：%s B：%s 价差：%s %%"
                                    .formatted(priceDiff.getPriceA(), priceDiff.getPriceB(), priceDiff.getDiffPlusAMinB()));
                                SpringUtils.getApplicationContext().publishEvent(ackSendAndSaveABOrderLogEvent);
                                //下单
                                handleOrder(abDisruptor, ABOrderSideType.PlusAminB);
                            }
                        }
                    }
                }
            }



            if (priceDiff.getDiffPlusAMinB().compareTo(operate.getCloseGap()) < 0) {
                if (Objects.nonNull(abOrderTask.getLastSellTimeStamp())
                    && ((currTime - abOrderTask.getLastSellTimeStamp()) / 1000) < operate.getColdSec()) {
                    log.info("在冷却时间内，暂停下单");
                } else {
                    //满足平仓
                    boolean a = checkSellPosition(positionByAccountA, operate, abOrderTask, "A", SideType.LONG);
                    boolean b = checkSellPosition(positionByAccountB, operate, abOrderTask, "B", SideType.SHORT);
                    if (a || b) {
                        //a/ b 任意一方有持仓，触发平仓
                        //可以下单
                        abOrderTask.setLastSellTimeStamp(currTime);
                        AckSendAndSaveABOrderLogEvent ackSendAndSaveABOrderLogEvent = new AckSendAndSaveABOrderLogEvent();
                        ackSendAndSaveABOrderLogEvent.setOrderTask(abOrderTask);
                        ackSendAndSaveABOrderLogEvent.setMsg("满足 +A -B 下单 A：%s B：%s 价差：%s %%"
                            .formatted(priceDiff.getPriceA(), priceDiff.getPriceB(), priceDiff.getDiffPlusAMinB()));
                        SpringUtils.getApplicationContext().publishEvent(ackSendAndSaveABOrderLogEvent);
                        //平仓
                        handleCloseOrder(abDisruptor, ABOrderSideType.PlusAminB);
                    }
                }
            }

        } else if (ABOrderSideType.PlusBminA.equalsIgnoreCase(type)) {
            //+B-A
            if("1".equalsIgnoreCase(operate.getReduceOnly())){
                //仅减仓 不触发下单

            }else{
                if (priceDiff.getDiffMinAPlusB().negate().compareTo(operate.getOpenGap()) >= 0) {
                    //满足下单
                    if (Objects.nonNull(abOrderTask.getLastOrderTimeStamp())
                        && ((currTime - abOrderTask.getLastOrderTimeStamp()) / 1000) < operate.getColdSec()) {
                        log.info("在冷却时间内，暂停下单");
                    } else {
                        //判断持仓
                        boolean a = checkOrderPosition(positionByAccountA, operate, abOrderTask, "A", SideType.SHORT);
                        if (a) {
                            boolean b = checkOrderPosition(positionByAccountB, operate, abOrderTask, "B", SideType.LONG);
                            if (b) {
                                //可以下单
                                abOrderTask.setLastOrderTimeStamp(currTime);
                                AckSendAndSaveABOrderLogEvent ackSendAndSaveABOrderLogEvent = new AckSendAndSaveABOrderLogEvent();
                                ackSendAndSaveABOrderLogEvent.setOrderTask(abOrderTask);
                                ackSendAndSaveABOrderLogEvent.setMsg("满足 +B -A 下单 A：%s B：%s 价差：%s %%"
                                    .formatted(priceDiff.getPriceA(), priceDiff.getPriceB(), priceDiff.getDiffMinAPlusB()));
                                SpringUtils.getApplicationContext().publishEvent(ackSendAndSaveABOrderLogEvent);
                                //下单
                                handleOrder(abDisruptor, ABOrderSideType.PlusBminA);
                            }
                        }
                    }
                }
            }



            if (priceDiff.getDiffMinAPlusB().negate().compareTo(operate.getCloseGap()) < 0) {
                //满足平仓
//                    coinsAbOrderLogService.sendAndSaveLog(abOrderTask, "满足 +B -A 平仓 A：%s B：%s 价差：%s %%"
//                        .formatted(priceDiff.getPriceA(), priceDiff.getPriceB(), priceDiff.getDiffMinAPlusB()));
                if (Objects.nonNull(abOrderTask.getLastSellTimeStamp())
                    && ((currTime - abOrderTask.getLastSellTimeStamp()) / 1000) < operate.getColdSec()) {
                    log.info("在冷却时间内，暂停下单");
                } else {
                    //满足平仓
                    boolean a = checkSellPosition(positionByAccountA, operate, abOrderTask, "A", SideType.LONG);
                    boolean b = checkSellPosition(positionByAccountB, operate, abOrderTask, "B", SideType.SHORT);
                    if (a || b) {
                        //a/ b 任意一方有持仓，触发平仓
                        //可以下单
                        abOrderTask.setLastSellTimeStamp(currTime);
                        AckSendAndSaveABOrderLogEvent ackSendAndSaveABOrderLogEvent = new AckSendAndSaveABOrderLogEvent();
                        ackSendAndSaveABOrderLogEvent.setOrderTask(abOrderTask);
                        ackSendAndSaveABOrderLogEvent.setMsg("满足 +B -A 平仓 A：%s B：%s 价差：%s %%"
                            .formatted(priceDiff.getPriceA(), priceDiff.getPriceB(), priceDiff.getDiffMinAPlusB()));
                        SpringUtils.getApplicationContext().publishEvent(ackSendAndSaveABOrderLogEvent);
                        //平仓
                        handleCloseOrder(abDisruptor, ABOrderSideType.PlusBminA);
                    }
                }
            }
        }
//        DisruptorExecutorService.submit(() -> {
//
//        });
    }

    /**
     * 下单
     * @param abDisruptor
     * @param type 方向 {@link ABOrderSideType}
     */
    private void handleOrder(ABDisruptor abDisruptor, String type) {
        ABOrderFrom abOrderFrom = new ABOrderFrom();
        ABOrderTask abOrderTask = abDisruptor.getAbOrderTask();
        BigDecimal leverage = Objects.isNull(abOrderTask.getLeverage()) ? BigDecimal.ONE : abOrderTask.getLeverage();
        ABOperate operate = abOrderTask.getOperate();

        //买入方向
        ArbitrageFormData sideA = new ArbitrageFormData();
        ArbitrageFormData sideB = new ArbitrageFormData();

        //统一市价
        sideA.setOrderType(OrderType.MARKET);
        sideA.setLeverage(leverage.longValue());
        sideA.setSize(operate.getSize());
        sideA.setAccountId(abOrderTask.getAccountA().getId());
        sideA.setExchange(abOrderTask.getExchangeA());
        sideA.setSymbol(abOrderTask.getSymbolA());

        sideB.setOrderType(OrderType.MARKET);
        sideB.setLeverage(leverage.longValue());
        sideB.setSize(operate.getSize());
        sideB.setAccountId(abOrderTask.getAccountB().getId());
        sideB.setExchange(abOrderTask.getExchangeB());
        sideB.setSymbol(abOrderTask.getSymbolB());
        if (type.equalsIgnoreCase(ABOrderSideType.PlusAminB)) {
            //+A -B
            abOrderFrom.setBuy(sideA);
            abOrderFrom.setSell(sideB);
        }else if(type.equalsIgnoreCase(ABOrderSideType.PlusBminA)){
            //+B -A
            abOrderFrom.setBuy(sideB);
            abOrderFrom.setSell(sideA);
        }else{
            throw new RuntimeException("方向异常");
        }
        abOrderFrom.setAbTaskId(abOrderTask.getTaskId());
        abOrderFrom.setUserId(abOrderTask.getUserId());
        abOrderFrom.setAbOrderTask(abOrderTask);
        SpringUtils.getApplicationContext().publishEvent(abOrderFrom);
    }


    /**
     * 平仓
     * @param abDisruptor
     * @param type 方向 {@link ABOrderSideType}
     */
    private void handleCloseOrder(ABDisruptor abDisruptor, String type) {
        ABCloseFrom abCloseFrom = new ABCloseFrom();
        ABOrderTask abOrderTask = abDisruptor.getAbOrderTask();
        BigDecimal leverage = Objects.isNull(abOrderTask.getLeverage()) ? BigDecimal.ONE : abOrderTask.getLeverage();
        ABOperate operate = abOrderTask.getOperate();

        //买入方向
        ArbitrageFormData sideA = new ArbitrageFormData();
        ArbitrageFormData sideB = new ArbitrageFormData();

        //统一市价
        sideA.setOrderType(OrderType.MARKET);
        sideA.setLeverage(leverage.longValue());
        sideA.setSize(operate.getSize());
        sideA.setAccountId(abOrderTask.getAccountA().getId());
        sideA.setExchange(abOrderTask.getExchangeA());
        sideA.setSymbol(abOrderTask.getSymbolA());

        sideB.setOrderType(OrderType.MARKET);
        sideB.setLeverage(leverage.longValue());
        sideB.setSize(operate.getSize());
        sideB.setAccountId(abOrderTask.getAccountB().getId());
        sideB.setExchange(abOrderTask.getExchangeB());
        sideB.setSymbol(abOrderTask.getSymbolB());

        if (type.equalsIgnoreCase(ABOrderSideType.PlusAminB)) {
            //+A -B
            abCloseFrom.setBuy(sideA);
            abCloseFrom.setSell(sideB);
        }else if(type.equalsIgnoreCase(ABOrderSideType.PlusBminA)){
            //+B -A
            abCloseFrom.setBuy(sideB);
            abCloseFrom.setSell(sideA);
        }else{
            throw new RuntimeException("方向异常");
        }
        abCloseFrom.setAbTaskId(abOrderTask.getTaskId());
        abCloseFrom.setUserId(abOrderTask.getUserId());
        abCloseFrom.setReduceOnly(true);
        abCloseFrom.setAbOrderTask(abOrderTask);
        SpringUtils.getApplicationContext().publishEvent(abCloseFrom);
    }

    private boolean checkOrderPosition(HashSet<PositionWsData> positionByAccount, ABOperate operate, ABOrderTask abOrderTask, String side,
                                       String sideType) {
        if (Objects.isNull(positionByAccount)) {
            return false;
        }
        for (PositionWsData positionWsData : positionByAccount) {
            if (positionWsData.getSymbol().equalsIgnoreCase(abOrderTask.getSymbolA())) {
                //找到对应的持仓了，判断持仓方向
                if (sideType.equalsIgnoreCase(positionWsData.getSide())) {
                    //判断是否达到最大持仓量
                    if (positionWsData.getSize().abs().compareTo(operate.getMaxSize()) < 0) {
                        //可以下单
                        return true;
                    } else {
                        log.warn("已达最大持仓仓位 当前仓位数量{} 最大仓位数量{} 任务：{} {} 币对:{}", positionWsData.getSide(), operate.getMaxSize(),
                            abOrderTask.getTaskId(), side, abOrderTask.getSymbolA());
                        return false;
                    }

                } else {
                    //仓位不对，不触发下单，只能平仓！
                    log.warn("仓位方向异常！任务：{} {} 币对：{}", abOrderTask.getTaskId(), side, abOrderTask.getSymbolA());
                    return false;
                }
            }
        }
        //没有持仓
        return true;
    }

    private boolean checkSellPosition(HashSet<PositionWsData> positionByAccount, ABOperate operate, ABOrderTask abOrderTask, String side,
                                      String sideType) {
        if (Objects.isNull(positionByAccount)) {
            return false;
        }
        for (PositionWsData positionWsData : positionByAccount) {
            if (positionWsData.getSymbol().equalsIgnoreCase(abOrderTask.getSymbolA())) {
                //找到对应的持仓了，判断持仓方向
                if (sideType.equalsIgnoreCase(positionWsData.getSide())) {
                    //判断仓位是否存在
                    if (positionWsData.getSize().abs().compareTo(BigDecimal.ZERO) > 0) {
                        //可以平仓
                        return true;
                    } else {
                        log.warn("持仓仓位为0 当前仓位数量{} 最大仓位数量{} 任务：{} {} 币对:{}", positionWsData.getSide(), operate.getMaxSize(),
                            abOrderTask.getTaskId(), side, abOrderTask.getSymbolA());
                        return false;
                    }

                } else {
                    //仓位不对，不触发！
//                    log.warn("仓位方向异常！任务：{} {} 币对：{}", abOrderTask.getTaskId(), side, abOrderTask.getSymbolA());
                    return false;
                }
            }
        }
        return false;
    }

    private PriceDiff calcPriceDiff(BigDecimal priceA, BigDecimal priceB) {
        //计算+A -B 价差
//        BigDecimal diffPlusAMinB = priceA.subtract(priceB).divide(priceB,8,BigDecimal.ROUND_DOWN).multiply(new BigDecimal(100)).setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal diffPlusAMinB = PriceDiffUtil.calcLongAShortB(priceA, priceB);
        //计算-A +B 价差
//        BigDecimal diffMinAPlusB = priceA.subtract(priceB).divide(priceB,8,BigDecimal.ROUND_DOWN).multiply(new BigDecimal(100)).setScale(4, BigDecimal.ROUND_HALF_UP);
        BigDecimal diffMinAPlusB = PriceDiffUtil.calcShortALongB(priceA, priceB);

        PriceDiff priceDiff = new PriceDiff();
        priceDiff.setDiffMinAPlusB(diffMinAPlusB);
        priceDiff.setDiffPlusAMinB(diffPlusAMinB);
        priceDiff.setPriceA(priceA);
        priceDiff.setPriceB(priceB);
        return priceDiff;
    }
}
