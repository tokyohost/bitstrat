package com.bitstrat.service.process;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bitstrat.constant.CoinsConstant;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.constant.SideType;
import com.bitstrat.constant.WebsocketMsgType;
import com.bitstrat.domain.*;
import com.bitstrat.domain.Event.AckAccountEvent;
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;
import com.bitstrat.domain.vo.CoinsOrderVo;
import com.bitstrat.domain.wsdomain.*;
import com.bitstrat.service.ExchangeWebsocketMessageProcess;
import com.bitstrat.service.ICoinsCrossExchangeArbitrageTaskService;
import com.bitstrat.service.ICoinsOrderService;
import com.bitstrat.service.WorkSpaceService;
import com.bitstrat.strategy.impl.BybitExchangeRestServiceImpl;
import com.bitstrat.wsClients.msg.receive.ADLWarning;
import com.bitstrat.wsClients.msg.receive.BybitClosePnlMessage;
import com.bitstrat.wsClients.msg.receive.BybitReceiveMessage;
import com.bitstrat.wsClients.msg.receive.LiqWarning;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.websocket.utils.WebSocketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/28 17:12
 * @Content 集中处理websocket 收到的仓位、订单变动数据
 */

@Component
@Slf4j
public class BybitExchangeWebsocketMessageProcessImpl implements ExchangeWebsocketMessageProcess {

    @Autowired
    ICoinsCrossExchangeArbitrageTaskService coinsCrossExchangeArbitrageTaskService;

    @Autowired
    BybitExchangeRestServiceImpl bybitExchangeRestService;
    @Autowired
    ICoinsOrderService coinsOrderService;

    @Autowired
    WorkSpaceService workSpaceService;

    @EventListener
    public void bybitPnlProcess(BybitClosePnlMessage pnlMessage) {
        CoinsOrderVo coinsOrderVo = coinsOrderService.queryByOrderId(pnlMessage.getOrderId());
        if (Objects.isNull(coinsOrderVo)) {
            log.error("bybit  订单号{} 收到平仓盈亏信息，但未找到对应订单号,盈亏信息 {}", pnlMessage.getOrderId(), JSONObject.toJSONString(pnlMessage));
            return;
        }
        LambdaUpdateWrapper<CoinsOrder> updatePnl = new LambdaUpdateWrapper<>();
        updatePnl.eq(CoinsOrder::getOrderId, pnlMessage.getOrderId());
        updatePnl.set(CoinsOrder::getPnl, pnlMessage.getPnlAmount());
        coinsOrderService.getBaseMapper().update(updatePnl);
        log.info("bybit 订单号 {} 已更新盈亏pnl 为 {}", pnlMessage.getOrderId(), pnlMessage.getPnlAmount());
    }

    @EventListener
    public void bybitProcess(BybitReceiveMessage bybitReceiveMessage) {
        log.debug("bybitReceiveMessage {}", JSONObject.toJSONString(bybitReceiveMessage));
        //todo 处理bybit仓位订单数据
        String msg = bybitReceiveMessage.getMsg();
        BybitWsMsgData bybitWsMsgData = JSONObject.parseObject(msg, BybitWsMsgData.class);
        if ("position".equalsIgnoreCase(bybitWsMsgData.getTopic())) {
            //仓位数据
            JSONArray datas = bybitWsMsgData.getData();
            for (Object data : datas) {
                JSONObject from = JSONObject.from(data);
                BybitPositionItem bybitPositionItem = from.to(BybitPositionItem.class);
                if (bybitPositionItem.getCategory().equalsIgnoreCase("linear")) {
                    if (bybitPositionItem.getPositionStatus().equalsIgnoreCase("Liq")) {
                        //仓位被强平！
                        LiqWarning liqWarning = LiqWarning.fromWsReceiveMsg(bybitReceiveMessage);
                        SpringUtils.getApplicationContext().publishEvent(liqWarning);
                    }
                    if (bybitPositionItem.getPositionStatus().equalsIgnoreCase("Adl")) {
                        //仓位被ADL 自动减仓！
                        ADLWarning adlWarning = ADLWarning.fromWsReceiveMsg(bybitReceiveMessage);
                        SpringUtils.getApplicationContext().publishEvent(adlWarning);
                    }

                    processLinerPosition(bybitPositionItem, Long.valueOf(bybitReceiveMessage.getUserId()), bybitReceiveMessage.getConnectionConfig().getAccount());
                }

            }

            syncProcessLinerPosition(datas, Long.valueOf(bybitReceiveMessage.getUserId()), bybitReceiveMessage.getConnectionConfig().getAccount());


        } else if ("execution".equalsIgnoreCase(bybitWsMsgData.getTopic())) {
            //订单数据
            JSONArray datas = bybitWsMsgData.getData();
            for (Object data : datas) {
                JSONObject from = JSONObject.from(data);
                BybitOrderItem bybitOrderItem = from.to(BybitOrderItem.class);
                processOrder(bybitOrderItem, Long.valueOf(bybitReceiveMessage.getUserId()));

            }

        } else if ("wallet".equalsIgnoreCase(bybitWsMsgData.getTopic())) {
            //处理账户数据
            JSONArray datas = bybitWsMsgData.getData();
            for (Object data : datas) {
                JSONObject from = JSONObject.from(data);
                BybitAccountItem bybitAccountItem = from.to(BybitAccountItem.class);
                /**
                 * // walletBalance - totalPositionIM - totalOrderIM - locked - bonus
                 *             BigDecimal totalEquity = unifiedData.getBigDecimal("totalEquity");
                 *             BigDecimal walletBalance = unifiedData.getBigDecimal("walletBalance");
                 *             BigDecimal totalPositionIM = unifiedData.getBigDecimal("totalPositionIM");
                 *             BigDecimal totalOrderIM = unifiedData.getBigDecimal("totalOrderIM");
                 *             BigDecimal bonus = unifiedData.getBigDecimal("bonus");
                 *             BigDecimal locked = unifiedData.getBigDecimal("locked");
                 *
                 *             accountBalance.setBalance(totalEquity);
                 *             accountBalance.setEquity(totalEquity);
                 *             accountBalance.setFreeBalance(walletBalance.subtract(totalPositionIM).subtract(totalOrderIM).subtract(locked).subtract(bonus));
                 *
                 */
                if (bybitAccountItem != null
                    && bybitAccountItem.getAccountType().equalsIgnoreCase("UNIFIED")) {
                    List<Coin> coin = bybitAccountItem.getCoin();
                    Account account = bybitReceiveMessage.getConnectionConfig().getAccount();
                    for (Coin coinitem : coin) {
                        if (CoinsConstant.USDT.equalsIgnoreCase(coinitem.getCoin())) {
                            AckAccountEvent ackAccountEvent = new AckAccountEvent();
                            ackAccountEvent.setAccount(account);
                            ackAccountEvent.setAccountId(account.getId());
                            ackAccountEvent.setBalance(bybitAccountItem.getTotalEquity());
                            BigDecimal walletBalance = coinitem.getWalletBalance();
                            BigDecimal totalPositionIM = coinitem.getTotalPositionIM();
                            BigDecimal totalOrderIM = coinitem.getTotalOrderIM();
                            BigDecimal bonus = coinitem.getBonus();
                            BigDecimal locked = coinitem.getLocked();
                            ackAccountEvent.setFreeBalance(walletBalance.subtract(totalPositionIM).subtract(totalOrderIM).subtract(locked).subtract(bonus));
                            SpringUtils.getApplicationContext().publishEvent(ackAccountEvent);
                        }

                    }


                }
            }

        }


    }

    /**
     * 处理订单成交
     *
     * @param bybitOrderItem
     * @param userId
     */
    private void processOrder(BybitOrderItem bybitOrderItem, Long userId) {
        CoinsOrderVo coinsOrderVo = coinsOrderService.queryByOrderId(bybitOrderItem.getOrderId());
        if (Objects.isNull(coinsOrderVo)) {
            log.error("bybit coinsOrderVo is null,order id = {}", bybitOrderItem.getOrderId());
            return;
        }

//        if (Objects.equals(coinsOrderVo.getOrderEnd(), OrderEndStatus.END)) {
//            log.warn("bybit 订单状态已结束 orderid={}",bybitOrderItem.getOrderId());
//            return;
//        }
        //找到需要查询的订单了,成交消息
        LambdaUpdateWrapper<CoinsOrder> orderUpdate = new LambdaUpdateWrapper<>();
        orderUpdate.eq(CoinsOrder::getId, coinsOrderVo.getId());
        //手续费
        String fee = coinsOrderVo.getFee();
        if (NumberUtils.isParsable(fee)) {
            BigDecimal oldFee = new BigDecimal(fee);
            BigDecimal feenew = oldFee.add(bybitOrderItem.getExecFee());
            orderUpdate.set(CoinsOrder::getFee, feenew.toPlainString());

        } else {
            orderUpdate.set(CoinsOrder::getFee, bybitOrderItem.getExecFee().toPlainString());
        }


        //成交数量
        String size = coinsOrderVo.getSize();
        if (NumberUtils.isParsable(size)) {
            BigDecimal oldSize = new BigDecimal(size);
            BigDecimal sizeNew = oldSize.add(bybitOrderItem.getExecQty());
            orderUpdate.set(CoinsOrder::getSize, sizeNew.toPlainString());
        } else {
            orderUpdate.set(CoinsOrder::getSize, bybitOrderItem.getExecQty().toPlainString());
        }

        //成交价格

        orderUpdate.set(CoinsOrder::getAvgPrice, bybitOrderItem.getExecPrice());
        //每笔平仓盈亏
        if (Objects.nonNull(bybitOrderItem.getExecPnl())) {
            orderUpdate.set(CoinsOrder::getPnl, bybitOrderItem.getExecPnl());
        } else {
            orderUpdate.set(CoinsOrder::getPnl, BigDecimal.ZERO);
        }

        //成交价值
        BigDecimal cumExecValue = coinsOrderVo.getCumExecValue();
        if (Objects.nonNull(cumExecValue)) {
            orderUpdate.set(CoinsOrder::getCumExecValue, cumExecValue.add(bybitOrderItem.getExecValue()));
        } else {
            orderUpdate.set(CoinsOrder::getCumExecValue, bybitOrderItem.getExecValue());
        }
        coinsOrderService.getBaseMapper().update(orderUpdate);
        BigDecimal leavesQty = bybitOrderItem.getLeavesQty();//剩余未成交数量
        if (leavesQty.doubleValue() == 0) {
            //完全成交
            //查询订单详情,触发订单同步
            coinsOrderService.syncOrder(List.of(coinsOrderVo), userId, false);
        }

        //发送订单给前端
        workSpaceService.sendOrderWebsocket(coinsOrderVo.getId());

    }

    /**
     * 处理合约仓位状态
     *
     * @param bybitPositionItem
     * @param userId
     */
    private void processLinerPosition(BybitPositionItem bybitPositionItem, Long userId, Account account) {
        String symbol = bybitPositionItem.getSymbol();
        if (symbol.endsWith("USDT")) {
            symbol = symbol.replaceAll("USDT(?=$|.*USDT)", "");
        }

        CoinsCrossExchangeArbitrageTaskVo taskVo = coinsCrossExchangeArbitrageTaskService.queryActiveTaskByUserIdSymbolAndAccountId(symbol, userId, account.getId());
        if (Objects.isNull(taskVo)) {
            log.info("未查找到对应的套利任务 userId:{} ex:{} symbol:{}", userId, ExchangeType.BYBIT.getName(), symbol);
            return;
        }
        if (taskVo.getLongEx().equalsIgnoreCase(ExchangeType.BYBIT.getName()) && Objects.equals(taskVo.getLongAccountId(), account.getId())) {
            //做多方
            if (bybitPositionItem.getSide().equalsIgnoreCase(SideType.BUY)) {
                processLongPosition(bybitPositionItem, taskVo);
            } else {
                log.error("bybit 币种 {} 持仓方向与任务方向不一致 持仓方向{}  任务方向 做多", symbol, bybitPositionItem.getSide());
            }

        } else if (taskVo.getShortEx().equalsIgnoreCase(ExchangeType.BYBIT.getName()) && Objects.equals(taskVo.getShortAccountId(), account.getId())) {
            //做空方
            if (bybitPositionItem.getSide().equalsIgnoreCase(SideType.SELL)) {
                processShortPosition(bybitPositionItem, taskVo);
            } else {
                log.error("bybit 币种 {} 持仓方向与任务方向不一致 持仓方向{}  任务方向 做空", symbol, bybitPositionItem.getSide());
            }
        }
        if (bybitPositionItem.getSide().equalsIgnoreCase("")) {
            //空，可能空仓了
            if (taskVo.getLongEx().equalsIgnoreCase(ExchangeType.BYBIT.getName())) {
                processLongPosition(bybitPositionItem, taskVo);
            } else if (taskVo.getShortEx().equalsIgnoreCase(ExchangeType.BYBIT.getName())) {
                processShortPosition(bybitPositionItem, taskVo);
            }

        }

        /**
         * OrderPosition orderPosition = new OrderPosition();
         *                         orderPosition.setSymbol(symbolPosition);
         *                         orderPosition.setAvgPrice(ordered.getBigDecimal("avgPrice"));
         *                         //Buy: 多头; Sell: 空头
         *                         orderPosition.setSide(ordered.getString("side").equalsIgnoreCase("buy") ? "long" : "short");
         *                         orderPosition.setSize(ordered.getBigDecimal("size"));
         *                         orderPosition.setEx(ExchangeType.BYBIT.getName());
         *                         orderPosition.setLever(ordered.getBigDecimal("leverage"));
         *                         orderPosition.setLiqPx(ordered.getBigDecimal("liqPrice"));
         *                         orderPosition.setFee(null);
         *                         orderPosition.setFundingFee(ordered.getBigDecimal("curRealisedPnl"));
         *                         orderPosition.setRealizedPnl(ordered.getBigDecimal("curRealisedPnl"));
         *                         BigDecimal unrealisedPnl = ordered.getBigDecimal("unrealisedPnl");
         *                         BigDecimal curRealisedPnl = ordered.getBigDecimal("curRealisedPnl");
         *                         if (Objects.nonNull(unrealisedPnl) && Objects.nonNull(curRealisedPnl)) {
         *                             orderPosition.setProfit(unrealisedPnl.add(curRealisedPnl));
         *                         }else{
         *                             orderPosition.setProfit(BigDecimal.ZERO);
         *                         }
         *                         orderPosition.setSettledPnl(ordered.getBigDecimal("curRealisedPnl"));
         * //                            orderPosition.setFee(BigDecimal.ZERO);
         * //                            orderPosition.setFundingFee(BigDecimal.ZERO);
         *                         orderPosition.setSymbol(sourceSymbol);
         */


    }

    private void processShortPosition(BybitPositionItem bybitPositionItem, CoinsCrossExchangeArbitrageTaskVo taskVo) {
        UpdateWrapper<CoinsCrossExchangeArbitrageTask> taskUpdateWrapper = new UpdateWrapper<>();
        LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask> lambda = taskUpdateWrapper.lambda();
        lambda.eq(CoinsCrossExchangeArbitrageTask::getId, taskVo.getId());
        lambda.set(CoinsCrossExchangeArbitrageTask::getShortSymbolSize, bybitPositionItem.getSize());
        lambda.set(CoinsCrossExchangeArbitrageTask::getShortAvgPrice, bybitPositionItem.getSessionAvgPrice());
        lambda.set(CoinsCrossExchangeArbitrageTask::getShortFundingFee, bybitPositionItem.getCurRealisedPnl());
        BigDecimal curRealisedPnl = bybitPositionItem.getCurRealisedPnl();
        BigDecimal unrealisedPnl = bybitPositionItem.getUnrealisedPnl();
        if (Objects.nonNull(unrealisedPnl) && Objects.nonNull(curRealisedPnl)) {
            lambda.set(CoinsCrossExchangeArbitrageTask::getShortProfit, unrealisedPnl.add(curRealisedPnl));
        } else {
            lambda.set(CoinsCrossExchangeArbitrageTask::getShortProfit, BigDecimal.ZERO);
        }
        coinsCrossExchangeArbitrageTaskService.getBaseMapper().update(lambda);
    }

    private void processLongPosition(BybitPositionItem bybitPositionItem, CoinsCrossExchangeArbitrageTaskVo taskVo) {
        UpdateWrapper<CoinsCrossExchangeArbitrageTask> taskUpdateWrapper = new UpdateWrapper<>();
        LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask> lambda = taskUpdateWrapper.lambda();
        lambda.eq(CoinsCrossExchangeArbitrageTask::getId, taskVo.getId());
        lambda.set(CoinsCrossExchangeArbitrageTask::getLongSymbolSize, bybitPositionItem.getSize());
        lambda.set(CoinsCrossExchangeArbitrageTask::getLongAvgPrice, bybitPositionItem.getSessionAvgPrice());
        lambda.set(CoinsCrossExchangeArbitrageTask::getLongFundingFee, bybitPositionItem.getCurRealisedPnl());
        BigDecimal curRealisedPnl = bybitPositionItem.getCurRealisedPnl();
        BigDecimal unrealisedPnl = bybitPositionItem.getUnrealisedPnl();
        if (Objects.nonNull(unrealisedPnl) && Objects.nonNull(curRealisedPnl)) {
            lambda.set(CoinsCrossExchangeArbitrageTask::getLongProfit, unrealisedPnl.add(curRealisedPnl));
        } else {
            lambda.set(CoinsCrossExchangeArbitrageTask::getLongProfit, BigDecimal.ZERO);
        }
        coinsCrossExchangeArbitrageTaskService.getBaseMapper().update(lambda);
    }


    private void syncProcessLinerPosition(JSONArray datas, Long userId, Account account) {
        List<PositionWsData> wsDatas = new ArrayList<>();
        for (Object data : datas) {
            JSONObject from = JSONObject.from(data);
            BybitPositionItem bybitPositionItem = from.to(BybitPositionItem.class);
            PositionWsData positionWsData = new PositionWsData();
            positionWsData.setExchange(ExchangeType.BYBIT.getName());
            positionWsData.setLeverage(bybitPositionItem.getLeverage());
            positionWsData.setAvgPrice(bybitPositionItem.getSessionAvgPrice());
            positionWsData.setSize(bybitPositionItem.getSize());
            String symbol = bybitPositionItem.getSymbol().replaceAll("USDT", "");
            if (bybitPositionItem.getSide().equalsIgnoreCase(SideType.BUY)) {
                positionWsData.setSide(SideType.LONG);
            } else if (bybitPositionItem.getSide().equalsIgnoreCase(SideType.SELL)) {
                positionWsData.setSide(SideType.SHORT);
            }

            positionWsData.setMarginType(bybitPositionItem.getTradeMode() == 0 ? "cross" : "isolated");
            positionWsData.setProfit(bybitPositionItem.getCurRealisedPnl());
            positionWsData.setSymbol(symbol);
            positionWsData.setUnrealizedProfit(bybitPositionItem.getUnrealisedPnl());
            positionWsData.setFee(null);
            positionWsData.setMarginPrice(bybitPositionItem.getPositionBalance());
            positionWsData.setLiqPrice(bybitPositionItem.getLiqPrice());
            positionWsData.setUpdateTime(new Date(bybitPositionItem.getUpdatedTime()));
            positionWsData.setServerTime(new Date());
            positionWsData.setAccountId(account.getId());
            positionWsData.setAccountName(account.getName());
            wsDatas.add(positionWsData);
        }
        PositionWebsocketMsgData<List<PositionWsData>> websocketMsgData = new PositionWebsocketMsgData<>();
        websocketMsgData.setData(wsDatas);
        websocketMsgData.setType(WebsocketMsgType.POSITION);
        websocketMsgData.setAccountId(account.getId());
        websocketMsgData.setUserId(account.getUserId());
        websocketMsgData.setExchangeName(ExchangeType.BYBIT.getName());
        SpringUtils.getApplicationContext().publishEvent(websocketMsgData);

    }

}
