package com.bitstrat.service.process;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bitstrat.constant.*;
import com.bitstrat.domain.*;
import com.bitstrat.domain.Event.AckAccountEvent;
import com.bitstrat.domain.bitget.BitgetAccountItem;
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;
import com.bitstrat.domain.vo.CoinsOrderVo;
import com.bitstrat.domain.wsdomain.*;
import com.bitstrat.service.BatchOrderTaskService;
import com.bitstrat.service.ExchangeWebsocketMessageProcess;
import com.bitstrat.service.ICoinsCrossExchangeArbitrageTaskService;
import com.bitstrat.service.ICoinsOrderService;
import com.bitstrat.store.OrderStatusConstant;
import com.bitstrat.utils.StringListUtil;
import com.bitstrat.wsClients.msg.receive.BitgetReceiveMessage;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.knowm.xchange.Exchange;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.bitstrat.constant.LockConstant.ORDER_LOCK;
import static com.bitstrat.constant.LockConstant.POSITION_SYNC_LOCK;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/28 17:12
 * @Content 集中处理websocket 收到的仓位、订单变动数据
 */

@Component
@Slf4j
public class BitgetExchangeWebsocketMessageProcessImpl implements ExchangeWebsocketMessageProcess {

    @Autowired
    ICoinsCrossExchangeArbitrageTaskService coinsCrossExchangeArbitrageTaskService;

    @Autowired
    ICoinsOrderService coinsOrderService;

    @Autowired
    RedissonClient redissonClient;
    Cache<String, BitgetOrderItem> cache;

    public BitgetExchangeWebsocketMessageProcessImpl() {
        cache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)  // 指定写入后 10s 过期
            .removalListener((String key, BitgetOrderItem value, RemovalCause cause) -> {
                if (cause == RemovalCause.EXPIRED) {
//                    log.info("BitgetExchange 过期通知 -> key: {}",key);
//                    log.info("过期通知 -> key: {}, value: {}",key,JSONObject.toJSONString(value));
                    if (Objects.nonNull(value)) {
                        //订单通知过期，继续尝试处理
                        processSwapOrder(value,value.getUserId());
                    }
                }
            })
            .build();
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(()->{
            cache.cleanUp();
        },1,2,TimeUnit.SECONDS);
    }

    @EventListener
    public void bitgetProcess(BitgetReceiveMessage bitgetReceiveMessage) {
        log.debug("bitgetReceiveMessage {}", JSONObject.toJSONString(bitgetReceiveMessage));
        //todo 处理bitget仓位订单数据
        //处理仓位数据
        String msg = bitgetReceiveMessage.getMsg();
        BitgetWsMsgData bitgetWsMsgData = JSONObject.parseObject(msg, BitgetWsMsgData.class);
        BitgetArg arg = bitgetWsMsgData.getArg();
        if(arg != null) {
            if(StringUtils.isNotEmpty(bitgetWsMsgData.getEvent()) && bitgetWsMsgData.getEvent().equalsIgnoreCase("subscribe")){
                return;
            }
            if("positions".equalsIgnoreCase(arg.getChannel())){
                //仓位数据
                JSONArray data = bitgetWsMsgData.getData();
                if (Objects.isNull(data)) {
                    log.error("msg data is null");
                    return;
                }
                if(arg.getInstType().equalsIgnoreCase("USDT-FUTURES")){

                    HashSet<String> positionHolder = new HashSet<>();
                    //合约
                    for (Object item : data) {
                        JSONObject from = JSONObject.from(item);
                        BitgetPositionItem bitgetPositionItem = from.to(BitgetPositionItem.class);
                        if(bitgetPositionItem != null) {
                            //合约持仓
                            processSwapPosition(bitgetPositionItem,Long.valueOf(bitgetReceiveMessage.getUserId()),bitgetReceiveMessage.getConnectionConfig().getAccount(),positionHolder);
                        }
                    }

                    if (data.isEmpty()) {
                        //剩下的就是没有持仓的了
                        processSwapPositionClosedAll(Long.valueOf(bitgetReceiveMessage.getUserId()), bitgetReceiveMessage.getConnectionConfig().getAccount(),positionHolder);
                    }
                    syncProcessSwapPosition(data,Long.valueOf(bitgetReceiveMessage.getUserId()),bitgetReceiveMessage.getConnectionConfig().getAccount(),positionHolder);


                }

            }
        //下单后订单状态通过websocket 通知现在暂时不需要
//            if("orders".equalsIgnoreCase(arg.getChannel())){
//                //处理订单数据
//                JSONArray data = bitgetWsMsgData.getData();
//                if (Objects.isNull(data)) {
//                    log.error("msg data is null");
//                    return;
//                }
//                if(arg.getInstType().equalsIgnoreCase("USDT-FUTURES")){
//                    //合约订单
//                    for (Object item : data) {
//                        JSONObject from = JSONObject.from(item);
//                        BitgetOrderItem bitgetOrderItem = from.to(BitgetOrderItem.class);
//                        if(bitgetOrderItem != null) {
//                            //合约订单
//                            processSwapOrder(bitgetOrderItem,Long.valueOf(bitgetReceiveMessage.getUserId()));
//                        }
//                    }
//                }
//
//            }

            //账户
            if("account".equalsIgnoreCase(arg.getChannel())){
                //处理账户数据
                JSONArray data = bitgetWsMsgData.getData();
                if (Objects.isNull(data)) {
                    log.error("account msg data is null");
                    return;
                }
                if(arg.getInstType().equalsIgnoreCase("USDT-FUTURES")){
                    processAccountSwap(data, bitgetReceiveMessage.getConnectionConfig().getAccount());
                }

            }
        }



    }

    /**
     * 处理账户数据
     *
     * {
     *         "marginCoin": "USDT",
     *         "frozen": "0.00000000",
     *         "available": "13.98545761",
     *         "maxOpenPosAvailable": "13.98545761",
     *         "maxTransferOut": "13.98545761",
     *         "equity": "13.98545761",
     *         "usdtEquity": "13.985457617660",
     *         "crossedRiskRate": "0",
     *         "unrealizedPL": "0.000000000000"
     *       }
     *
     *       accountBalance.setSymbol(coinItem.getString("marginCoin"));
     *                     accountBalance.setBalance(coinItem.getBigDecimal("available"));
     *                     accountBalance.setEquity(coinItem.getBigDecimal("equity"));
     *                     accountBalance.setFreeBalance(coinItem.getBigDecimal("crossMaxAvailable"));
     * @param data
     * @param account
     */
    private void processAccountSwap(JSONArray data, Account account) {
        for (Object datum : data) {
            JSONObject from = JSONObject.from(datum);
            BitgetAccountItem bitgetAccountItem = from.to(BitgetAccountItem.class);
            if(bitgetAccountItem != null) {
                if (CoinsConstant.USDT.equalsIgnoreCase(bitgetAccountItem.getMarginCoin())) {
                    AckAccountEvent ackAccountEvent = new AckAccountEvent();
                    ackAccountEvent.setAccountId(account.getId());
                    ackAccountEvent.setAccount(account);
                    ackAccountEvent.setBalance(bitgetAccountItem.getAvailable());
                    ackAccountEvent.setFreeBalance(bitgetAccountItem.getMaxOpenPosAvailable());
                    SpringUtils.getApplicationContext().publishEvent(ackAccountEvent);
                }
            }
        }



    }

    private void syncProcessSwapPosition(JSONArray data, Long userId, Account account, HashSet<String> positionHolder) {
        List<PositionWsData> wsDatas = new ArrayList<>();
        for (Object item : data) {
            JSONObject from = JSONObject.from(item);
            BitgetPositionItem bitgetPositionItem = from.to(BitgetPositionItem.class);
            if (bitgetPositionItem != null) {
                String instId = bitgetPositionItem.getInstId();
                String symbol = instId.replaceAll("USDT", "");

                PositionWsData positionWsData = new PositionWsData();
                positionWsData.setSymbol(symbol);
                positionWsData.setExchange(ExchangeType.BITGET.getName());
                positionWsData.setAccountId(account.getId());
                positionWsData.setLiqPrice(bitgetPositionItem.getLiquidationPrice());

                if (bitgetPositionItem.getHoldSide().equalsIgnoreCase(SideType.SHORT)) {
                    positionWsData.setSide(SideType.SHORT);
                }else if(bitgetPositionItem.getHoldSide().equalsIgnoreCase(SideType.LONG)){
                    positionWsData.setSide(SideType.LONG);
                }
                positionWsData.setPosType(PositionType.SWAP);
                positionWsData.setFee(bitgetPositionItem.getDeductedFee());
                positionWsData.setSize(bitgetPositionItem.getTotal());
                positionWsData.setFundingFee(bitgetPositionItem.getTotalFee());
                positionWsData.setProfit(bitgetPositionItem.getAchievedProfits());
                positionWsData.setUnrealizedProfit(bitgetPositionItem.getUnrealizedPL());
                positionWsData.setMarginPrice(bitgetPositionItem.getMarginSize());
                positionWsData.setMarginRatio(bitgetPositionItem.getKeepMarginRate());
                positionWsData.setLeverage(bitgetPositionItem.getLeverage());
                positionWsData.setMarginType(bitgetPositionItem.getMarginMode());
                positionWsData.setAvgPrice(bitgetPositionItem.getOpenPriceAvg());
                positionWsData.setUpdateTime(new Date(bitgetPositionItem.getUTime()));
                positionWsData.setAccountName(account.getName());
                positionWsData.setServerTime(new Date());
                wsDatas.add(positionWsData);
            }
        }
        PositionWebsocketMsgData<List<PositionWsData>> websocketMsgData = new PositionWebsocketMsgData<>();
        websocketMsgData.setData(wsDatas);
        websocketMsgData.setType(WebsocketMsgType.POSITION);
        websocketMsgData.setAccountId(account.getId());
        websocketMsgData.setUserId(account.getUserId());
        websocketMsgData.setExchangeName(ExchangeType.BITGET.getName());
        SpringUtils.getApplicationContext().publishEvent(websocketMsgData);

    }

    /**
     * 全部平仓
     * @param userId
     * @param account
     */
    private void processSwapPositionClosedAll(Long userId, Account account,HashSet<String> positionHolder) {
        List<CoinsCrossExchangeArbitrageTaskVo> coinsCrossExchangeArbitrageTaskVos = coinsCrossExchangeArbitrageTaskService.queryActiveTaskByUserIdAndAccountId(userId, account.getId());
        for (CoinsCrossExchangeArbitrageTaskVo coinsCrossExchangeArbitrageTaskVo : coinsCrossExchangeArbitrageTaskVos) {
            boolean holderflag = false;
            for (String holder : positionHolder) {
                String replaced = coinsCrossExchangeArbitrageTaskVo.getLongSymbol().replace("USDT", "").replace("/", "");
                if (replaced.equalsIgnoreCase(holder)) {
                    //存在持仓
                    holderflag = true;
                }
                String replacedShort = coinsCrossExchangeArbitrageTaskVo.getShortSymbol().replace("USDT", "").replace("/", "");
                if (replacedShort.equalsIgnoreCase(holder)) {
                    //存在持仓
                    holderflag = true;
                }
            }
            if(holderflag){
                continue;
            }


            RLock lock = redissonClient.getLock(POSITION_SYNC_LOCK);
            lock.lock(30, TimeUnit.SECONDS);
            try{
                if (coinsCrossExchangeArbitrageTaskVo.getLongAccountId().equals(account.getId())) {
                    LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(CoinsCrossExchangeArbitrageTask::getId, coinsCrossExchangeArbitrageTaskVo.getId());
                    updateWrapper.set(CoinsCrossExchangeArbitrageTask::getLongSymbolSize,BigDecimal.ZERO);
                    coinsCrossExchangeArbitrageTaskService.getBaseMapper().update(updateWrapper);
                    //不存在持仓了
                    log.info("bitget 已无对应持仓 {} ",coinsCrossExchangeArbitrageTaskVo.getLongEx());
                }
                if (coinsCrossExchangeArbitrageTaskVo.getShortAccountId().equals(account.getId())) {
                    LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(CoinsCrossExchangeArbitrageTask::getId, coinsCrossExchangeArbitrageTaskVo.getId());
                    updateWrapper.set(CoinsCrossExchangeArbitrageTask::getShortSymbolSize,BigDecimal.ZERO);
                    coinsCrossExchangeArbitrageTaskService.getBaseMapper().update(updateWrapper);
                    //不存在持仓了
                    log.info("bitget 已无对应持仓 {} ",coinsCrossExchangeArbitrageTaskVo.getShortSymbol());
                }

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }

        }

    }

    private synchronized void processSwapOrder(BitgetOrderItem bitgetOrderItem, Long userId) {
        CoinsOrderVo coinsOrderVo = coinsOrderService.queryByOrderId(bitgetOrderItem.getClientOid());
        if(Objects.isNull(coinsOrderVo)){
            //订单还没有入库，但是websocket 已经更新了订单状态
            bitgetOrderItem.setUserId(userId);
            cache.put(bitgetOrderItem.getClientOid(),bitgetOrderItem);
            return;
        }


        RLock lock = redissonClient.getLock(ORDER_LOCK + ":" + userId + ":"+coinsOrderVo.getId());
        try {
            boolean b = lock.tryLock(25, TimeUnit.SECONDS);
            if (b) {
                coinsOrderVo = coinsOrderService.queryByOrderId(bitgetOrderItem.getClientOid());
                if(Objects.isNull(coinsOrderVo)) {
                    log.error("bitget coinsOrderVo is null,order id = {}", bitgetOrderItem.getClientOid());
                    return;
                }

                if (Objects.equals(coinsOrderVo.getOrderEnd(), OrderEndStatus.END)) {
                    log.warn("订单状态已结束 orderid={}",bitgetOrderItem.getClientOid());
                    return;
                }
                //中间态订单状态
                List<String> bitgetProcessStatus = OrderStatusConstant.bitgetProcessStatus;
                List<String> bitgetEndStatus = OrderStatusConstant.bitgetEndStatus;
                //订单状态
                /**
                 * 订单状态
                 * canceled：撤单成功
                 * live：等待成交
                 * partially_filled：部分成交
                 * filled：完全成交
                 * mmp_canceled：做市商保护机制导致的自动撤单
                 */
                String status = bitgetOrderItem.getStatus();
                LambdaUpdateWrapper<CoinsOrder> orderUpdate = new LambdaUpdateWrapper<>();
                orderUpdate.eq(CoinsOrder::getId, coinsOrderVo.getId());
                orderUpdate.set(CoinsOrder::getEx,ExchangeType.BITGET.getName());
//        orderUpdate.set(CoinsOrderVo::getOrderId,bitgetOrderItem.getOrdId());
//                            String posSide = datumFrom.getString("posSide");
//                            if(StringUtils.isNotBlank(posSide)){
//                                contractOrder.setSide("long".equalsIgnoreCase(posSide) ? SideType.LONG : SideType.SHORT);
//                            }else{
//                                String orderSide = datumFrom.getString("side");
//                                if(StringUtils.isNotBlank(orderSide)){
//                                    contractOrder.setSide("buy".equalsIgnoreCase(orderSide) ? SideType.BUY : SideType.SELL);
//                                }
//                            }
                if(StringUtils.isNotEmpty(bitgetOrderItem.getPrice())) {
                    orderUpdate.set(CoinsOrder::getPrice,bitgetOrderItem.getPrice());
                }
                orderUpdate.set(CoinsOrder::getSize,bitgetOrderItem.getSize());
                orderUpdate.set(CoinsOrder::getStatus,status.toLowerCase());
                //累计成交数量
                orderUpdate.set(CoinsOrder::getCumExecQty,bitgetOrderItem.getAccBaseVolume());
                orderUpdate.set(CoinsOrder::getAvgPrice,bitgetOrderItem.getPriceAvg());
//        contractOrder.setAvgPrice(bitgetOrderItem.getAvgPx());
//                List<FeeDetail> feeDetail = bitgetOrderItem.getFeeDetail();
//                BigDecimal totalFee = feeDetail.stream().map(FeeDetail::getFee).reduce(BigDecimal.ZERO, BigDecimal::add);
                orderUpdate.set(CoinsOrder::getFee,bitgetOrderItem.getFillFee());
                if(Objects.nonNull(bitgetOrderItem.getPnl())){
                    orderUpdate.set(CoinsOrder::getPnl,bitgetOrderItem.getPnl());
                }
//        contractOrder.setFee(bitgetOrderItem.getFee());

                boolean end = false;
                if(StringListUtil.containsIgnoreCase(bitgetProcessStatus, status)){
                    //中间态
                    orderUpdate.set(CoinsOrder::getOrderEnd, OrderEndStatus.NOT_END);
                }else if(StringListUtil.containsIgnoreCase(bitgetEndStatus, status)){
                    //结束态
                    orderUpdate.set(CoinsOrder::getOrderEnd, OrderEndStatus.END);
                    if (Objects.nonNull(bitgetOrderItem.getPriceAvg())) {
                        orderUpdate.set(CoinsOrder::getCumExecValue,bitgetOrderItem.getAccBaseVolume().multiply(bitgetOrderItem.getPriceAvg()));
                    }
                    end = true;

                }
                coinsOrderService.getBaseMapper().update(orderUpdate);
                if (coinsOrderVo.getBatchId() != null && end) {
                    //是批次单并且结束了
                    SpringUtils.getBean(BatchOrderTaskService.class).checkAndRunBatchOrderTask();
                }
            }
        } catch (InterruptedException e) {
            log.error("processSwapOrder error", e);
        }finally {
            lock.unlock();
        }


    }

    private synchronized void processSwapPosition(BitgetPositionItem bitgetPositionItem, Long userId, Account account,HashSet<String> syncedSymbol) {
        String instId = bitgetPositionItem.getInstId();
        String symbol = instId.replaceAll("USDT", "");
        syncedSymbol.add(symbol);
    }
}
