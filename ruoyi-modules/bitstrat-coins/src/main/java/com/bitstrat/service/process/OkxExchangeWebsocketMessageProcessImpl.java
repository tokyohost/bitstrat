package com.bitstrat.service.process;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.constant.OrderEndStatus;
import com.bitstrat.constant.SideType;
import com.bitstrat.constant.WebsocketMsgType;
import com.bitstrat.domain.*;
import com.bitstrat.domain.Event.AckAccountEvent;
import com.bitstrat.domain.okx.OkxAccountItem;
import com.bitstrat.domain.okx.OkxArg;
import com.bitstrat.domain.okx.OkxWsMarketMsgData;
import com.bitstrat.domain.okx.OkxWsMsgData;
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;
import com.bitstrat.domain.vo.CoinsOrderVo;
import com.bitstrat.domain.wsdomain.*;
import com.bitstrat.service.*;
import com.bitstrat.store.ExecuteService;
import com.bitstrat.store.OrderStatusConstant;
import com.bitstrat.strategy.impl.OkxExchangeRestServiceImpl;
import com.bitstrat.utils.StringListUtil;
import com.bitstrat.wsClients.msg.receive.BybitReceiveMessage;
import com.bitstrat.wsClients.msg.receive.OkxReceiveMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.websocket.utils.WebSocketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
public class OkxExchangeWebsocketMessageProcessImpl implements ExchangeWebsocketMessageProcess {

    @Autowired
    ICoinsCrossExchangeArbitrageTaskService coinsCrossExchangeArbitrageTaskService;

    @Autowired
    OkxExchangeRestServiceImpl okxExchangeRestService;

    @Autowired
    ICoinsOrderService coinsOrderService;
    @Autowired
    ExecuteService executeService;

    @Autowired
    WorkSpaceService workSpaceService;

    @EventListener
    public void okxProcess(OkxReceiveMessage okxReceiveMessage) {
        log.debug("okxReceiveMessage {}", JSONObject.toJSONString(okxReceiveMessage));
        //todo 处理okx仓位订单数据
        //处理仓位数据
        String msg = okxReceiveMessage.getMsg();
        OkxWsMsgData okxWsMsgData = JSONObject.parseObject(msg, OkxWsMsgData.class);
        OkxArg arg = okxWsMsgData.getArg();
        if (arg != null) {
            if (StringUtils.isNotEmpty(okxWsMsgData.getEvent()) && okxWsMsgData.getEvent().equalsIgnoreCase("subscribe")) {
                return;
            }
            //处理仓位
            if ("positions".equalsIgnoreCase(arg.getChannel())) {
                //仓位数据
                JSONArray data = okxWsMsgData.getData();
                if (Objects.isNull(data)) {
                    log.error("msg data is null");
                    return;
                }
                JSONArray okxPositionItems = new JSONArray();
                for (Object item : data) {
                    JSONObject from = JSONObject.from(item);
                    OkxPositionItem okxPositionItem = from.to(OkxPositionItem.class);
//                    log.info("okxPositionItem {}", okxPositionItem);
//                    if (okxPositionItem != null) {
//                        String instType = okxPositionItem.getInstType();
//                        if (instType.equalsIgnoreCase("SWAP")) {
//                            // 如果这个币对存在策略机器人，则发布策略机器人仓位变动事件
//
//                            //合约持仓
//                            processSwapPosition(okxPositionItem, Long.valueOf(okxReceiveMessage.getUserId()), okxReceiveMessage.getConnectionConfig().getAccount());
//
//                        }
//                    }
                    //处理止盈止损
                    okxPositionItem = processTpSl(okxPositionItem);
                    okxPositionItems.add(okxPositionItem);
                }

                //同步前端
                syncProcessSwapPosition(okxPositionItems, Long.valueOf(okxReceiveMessage.getUserId()), okxReceiveMessage.getConnectionConfig().getAccount());

            }

            //处理订单
            if ("orders".equalsIgnoreCase(arg.getChannel())) {
                //处理订单数据
                JSONArray data = okxWsMsgData.getData();
                if (Objects.isNull(data)) {
                    log.error("msg data is null");
                    return;
                }
                for (Object item : data) {
                    JSONObject from = JSONObject.from(item);
                    OkxOrderItem okxOrderItem = from.to(OkxOrderItem.class);
                    if (okxOrderItem != null) {
                        String instType = okxOrderItem.getInstType();
                        if (instType.equalsIgnoreCase("SWAP")) {
                            //合约订单
                            processSwapOrder(okxOrderItem, Long.valueOf(okxReceiveMessage.getUserId()), okxReceiveMessage.getConnectionConfig().getAccount());
                        }
                    }
                }
            }
            //处理市价
            if ("mark-price".equalsIgnoreCase(arg.getChannel())) {
                //市价
                SpringUtils.getApplicationContext().publishEvent(JSONObject.parseObject(msg, OkxWsMarketMsgData.class));
            }
            //处理账户
            if("account".equalsIgnoreCase(arg.getChannel())) {
                JSONArray data = okxWsMsgData.getData();
                if (Objects.isNull(data)) {
                    log.error("account msg data is null");
                    return;
                }

                processUSDTAccount(data,okxReceiveMessage.getConnectionConfig().getAccount());
            }
        }


    }

    private OkxPositionItem processTpSl(OkxPositionItem okxPositionItem) {
        List<OkxOrderAlgo> closeOrderAlgo = okxPositionItem.getCloseOrderAlgo();
        if(CollectionUtils.isEmpty(closeOrderAlgo)){
            return okxPositionItem;
        }
        for (OkxOrderAlgo orderAlgo : closeOrderAlgo) {
            if(Objects.nonNull(orderAlgo.getSlTriggerPx())){
                okxPositionItem.setStopLoss(orderAlgo.getSlTriggerPx().stripTrailingZeros());
            }
            if(Objects.nonNull(orderAlgo.getTpTriggerPx())){
                okxPositionItem.setTakeProfit(orderAlgo.getTpTriggerPx().stripTrailingZeros());
            }
        }
        return okxPositionItem;

    }

    private void processUSDTAccount(JSONArray data, Account account) {
        for (Object datum : data) {
            JSONObject from = JSONObject.from(datum);
            OkxAccountItem okxAccountItem = from.to(OkxAccountItem.class);
            if (okxAccountItem != null) {
                if("USDT".equals(okxAccountItem.getCcy())) {
                    AckAccountEvent ackAccountEvent = new AckAccountEvent();
                    ackAccountEvent.setAccount(account);

                    /**
                     * accountBalance.setBalance(ccyFrom.getBigDecimal("cashBal"));
                     *                         accountBalance.setEquity(ccyFrom.getBigDecimal("availEq"));
                     *                         accountBalance.setFreeBalance(ccyFrom.getBigDecimal("availBal"));
                     *                         accountBalance.setCashBalance(ccyFrom.getBigDecimal("cashBal"));
                     */
                    ackAccountEvent.setBalance(okxAccountItem.getCashBal());
                    ackAccountEvent.setFreeBalance(okxAccountItem.getAvailBal());
                    ackAccountEvent.setAccountId(account.getId());
                    SpringUtils.getApplicationContext().publishEvent(ackAccountEvent);
                }
            }
        }


    }

    private void processSwapOrder(OkxOrderItem okxOrderItem, Long userId, Account account) {
        CoinsOrderVo coinsOrderVo = coinsOrderService.queryByOrderId(okxOrderItem.getOrdId());
        if (Objects.isNull(coinsOrderVo)) {
            log.error("okx coinsOrderVo is null,order id = {}", okxOrderItem.getOrdId());
            return;
        }

        if (Objects.equals(coinsOrderVo.getOrderEnd(), OrderEndStatus.END)) {
            log.warn("订单状态已结束 orderid={}", okxOrderItem.getOrdId());
            return;
        }
        //中间态订单状态
        List<String> okxProcessStatus = OrderStatusConstant.okxProcessStatus;
        List<String> okxEndStatus = OrderStatusConstant.okxEndStatus;
        //订单状态
        /**
         * 订单状态
         * canceled：撤单成功
         * live：等待成交
         * partially_filled：部分成交
         * filled：完全成交
         * mmp_canceled：做市商保护机制导致的自动撤单
         */
        String state = okxOrderItem.getState();
        LambdaUpdateWrapper<CoinsOrder> orderUpdate = new LambdaUpdateWrapper<>();
        orderUpdate.eq(CoinsOrder::getId, coinsOrderVo.getId());
        orderUpdate.set(CoinsOrder::getEx, ExchangeType.OKX.getName());
//        orderUpdate.set(CoinsOrderVo::getOrderId,okxOrderItem.getOrdId());
//                            String posSide = datumFrom.getString("posSide");
//                            if(StringUtils.isNotBlank(posSide)){
//                                contractOrder.setSide("long".equalsIgnoreCase(posSide) ? SideType.LONG : SideType.SHORT);
//                            }else{
//                                String orderSide = datumFrom.getString("side");
//                                if(StringUtils.isNotBlank(orderSide)){
//                                    contractOrder.setSide("buy".equalsIgnoreCase(orderSide) ? SideType.BUY : SideType.SELL);
//                                }
//                            }
        orderUpdate.set(CoinsOrder::getPrice, okxOrderItem.getPx());
        orderUpdate.set(CoinsOrder::getSize, okxOrderItem.getSz());
        orderUpdate.set(CoinsOrder::getStatus, state.toLowerCase());
        //累计成交数量
        orderUpdate.set(CoinsOrder::getCumExecQty, okxOrderItem.getAccFillSz());
        orderUpdate.set(CoinsOrder::getAvgPrice, okxOrderItem.getAvgPx());
//        contractOrder.setAvgPrice(okxOrderItem.getAvgPx());
        orderUpdate.set(CoinsOrder::getFee, okxOrderItem.getFee());
        if (Objects.nonNull(okxOrderItem.getPnl())) {
            orderUpdate.set(CoinsOrder::getPnl, okxOrderItem.getPnl());
        }
//        contractOrder.setFee(okxOrderItem.getFee());

        boolean end = false;
        if (StringListUtil.containsIgnoreCase(okxProcessStatus, state)) {
            //中间态
            orderUpdate.set(CoinsOrder::getOrderEnd, OrderEndStatus.NOT_END);
        } else if (StringListUtil.containsIgnoreCase(okxEndStatus, state)) {
            //结束态
            orderUpdate.set(CoinsOrder::getOrderEnd, OrderEndStatus.END);
            if (Objects.nonNull(okxOrderItem.getAvgPx())) {
                BigDecimal size = okxExchangeRestService.calcShowSize(coinsOrderVo.getSymbol(), okxOrderItem.getSz());
                orderUpdate.set(CoinsOrder::getCumExecValue, size.multiply(okxOrderItem.getAvgPx()));
            }
            end = true;

        }
        coinsOrderService.getBaseMapper().update(orderUpdate);
        if (coinsOrderVo.getBatchId() != null && end) {
            //是批次单并且结束了
            SpringUtils.getBean(BatchOrderTaskService.class).checkAndRunBatchOrderTask();
        }
        //发送订单给前端
        workSpaceService.sendOrderWebsocket(coinsOrderVo.getId());
    }

    private void syncProcessSwapPosition(JSONArray data, Long userId, Account account) {
        ArrayList<PositionWsData> wsData = new ArrayList<>();

        for (Object item : data) {
            JSONObject from = JSONObject.from(item);
            OkxPositionItem okxPositionItem = from.to(OkxPositionItem.class);
            String instId = okxPositionItem.getInstId();
            String symbol = instId.replaceAll("-USDT-SWAP", "");


            PositionWsData positionWsData = new PositionWsData();
            positionWsData.setExchange(ExchangeType.OKX.getName());
            positionWsData.setSymbol(symbol);
            positionWsData.setSize(okxExchangeRestService.calcShowSize(symbol,okxPositionItem.getPos()));
            positionWsData.setAvgPrice(okxPositionItem.getAvgPx());
            positionWsData.setFundingFee(okxPositionItem.getFundingFee());
            positionWsData.setUnrealizedProfit(okxPositionItem.getUpl());
            positionWsData.setProfit(okxPositionItem.getRealizedPnl());
            positionWsData.setMarginType(okxPositionItem.getMgnMode());
            positionWsData.setMarginRatio(okxPositionItem.getMmr());
            positionWsData.setMarginPrice(okxPositionItem.getMargin());
            positionWsData.setFee(okxPositionItem.getFee());
            positionWsData.setUpdateTime(new Date(okxPositionItem.getUTime()));
            if(Objects.nonNull(okxPositionItem.getTakeProfit())){
                positionWsData.setTakeProfit(okxPositionItem.getTakeProfit().stripTrailingZeros());
            }
            if(Objects.nonNull(okxPositionItem.getStopLoss())){
                positionWsData.setStopLoss(okxPositionItem.getStopLoss().stripTrailingZeros());
            }

            if (SideType.LONG.equalsIgnoreCase(okxPositionItem.getPosSide())) {
                positionWsData.setSide(SideType.LONG);
            } else if (SideType.SHORT.equalsIgnoreCase(okxPositionItem.getPosSide())) {
                positionWsData.setSide(SideType.SHORT);
            }
            positionWsData.setAccountName(account.getName());
            positionWsData.setLeverage(okxPositionItem.getLever());
            positionWsData.setAccountId(account.getId());
            positionWsData.setLiqPrice(okxPositionItem.getLiqPx());
            positionWsData.setServerTime(new Date());
            positionWsData.setTakeProfit(okxPositionItem.getTakeProfit());
            positionWsData.setStopLoss(okxPositionItem.getStopLoss());
            wsData.add(positionWsData);
        }

        PositionWebsocketMsgData<List<PositionWsData>> websocketMsgData = new PositionWebsocketMsgData<>();
        websocketMsgData.setData(wsData);
        websocketMsgData.setType(WebsocketMsgType.POSITION);
        websocketMsgData.setAccountId(account.getId());
        websocketMsgData.setUserId(account.getUserId());
        websocketMsgData.setExchangeName(ExchangeType.OKX.getName());
        SpringUtils.getApplicationContext().publishEvent(websocketMsgData);

    }

    private void processSwapPosition(OkxPositionItem okxPositionItem, Long userId, Account account) {
        String instId = okxPositionItem.getInstId();
        String symbol = instId.replaceAll("-USDT-SWAP", "");


        CoinsCrossExchangeArbitrageTaskVo taskVo = coinsCrossExchangeArbitrageTaskService.queryActiveTaskByUserIdSymbolAndAccountId(symbol, userId, account.getId());
        if (Objects.isNull(taskVo)) {
            log.info("未查找到对应的套利任务 userId:{} ex:{} symbol:{}", userId, ExchangeType.OKX.getName(), symbol);
            return;
        }
        if (taskVo.getLongEx().equalsIgnoreCase(ExchangeType.OKX.getName()) && Objects.equals(taskVo.getLongAccountId(), account.getId())) {
            //做多方
            if (okxPositionItem.getPosSide().equalsIgnoreCase(SideType.LONG)) {
                UpdateWrapper<CoinsCrossExchangeArbitrageTask> taskUpdateWrapper = new UpdateWrapper<>();
                LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask> lambda = taskUpdateWrapper.lambda();
                lambda.eq(CoinsCrossExchangeArbitrageTask::getId, taskVo.getId());
                lambda.set(CoinsCrossExchangeArbitrageTask::getLongSymbolSize, okxPositionItem.getPos());
                lambda.set(CoinsCrossExchangeArbitrageTask::getLongAvgPrice, okxPositionItem.getAvgPx());
                lambda.set(CoinsCrossExchangeArbitrageTask::getLongFundingFee, okxPositionItem.getFundingFee());
                lambda.set(CoinsCrossExchangeArbitrageTask::getLongProfit, okxPositionItem.getUpl());
                coinsCrossExchangeArbitrageTaskService.getBaseMapper().update(lambda);

            } else {
                log.error("okx 币种 {} 持仓方向与任务方向不一致 持仓方向{}  任务方向 做多", symbol, okxPositionItem.getPosSide());
            }

        } else if (taskVo.getShortEx().equalsIgnoreCase(ExchangeType.OKX.getName()) && Objects.equals(taskVo.getShortAccountId(), account.getId())) {
            //做空方
            if (okxPositionItem.getPosSide().equalsIgnoreCase(SideType.SHORT)) {
                UpdateWrapper<CoinsCrossExchangeArbitrageTask> taskUpdateWrapper = new UpdateWrapper<>();
                LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask> lambda = taskUpdateWrapper.lambda();
                lambda.eq(CoinsCrossExchangeArbitrageTask::getId, taskVo.getId());
                lambda.set(CoinsCrossExchangeArbitrageTask::getShortSymbolSize, okxPositionItem.getPos());
                lambda.set(CoinsCrossExchangeArbitrageTask::getShortAvgPrice, okxPositionItem.getAvgPx());
                lambda.set(CoinsCrossExchangeArbitrageTask::getShortFundingFee, okxPositionItem.getFundingFee());
                lambda.set(CoinsCrossExchangeArbitrageTask::getShortProfit, okxPositionItem.getUpl());
                coinsCrossExchangeArbitrageTaskService.getBaseMapper().update(lambda);
            } else {
                log.error("okx 币种 {} 持仓方向与任务方向不一致 持仓方向{}  任务方向 做空", symbol, okxPositionItem.getPosSide());
            }
        }


    }
}
