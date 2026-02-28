package com.bitstrat.service.process;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bitstrat.constant.*;
import com.bitstrat.domain.*;
import com.bitstrat.domain.Event.AckAccountEvent;
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;
import com.bitstrat.domain.vo.CoinsOrderVo;
import com.bitstrat.service.*;
import com.bitstrat.store.ExecuteService;
import com.bitstrat.store.OrderStatusConstant;
import com.bitstrat.strategy.impl.BinanceExchangeRestServiceImpl;
import com.bitstrat.utils.StringListUtil;
import com.bitstrat.wsClients.msg.receive.ADLWarning;
import com.bitstrat.wsClients.msg.receive.FBinanceReceiveMessage;
import com.bitstrat.wsClients.msg.receive.LiqWarning;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.websocket.utils.WebSocketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
public class BinanceExchangeWebsocketMessageProcessImpl implements ExchangeWebsocketMessageProcess {

    @Autowired
    ICoinsCrossExchangeArbitrageTaskService coinsCrossExchangeArbitrageTaskService;


    @Autowired
    ICoinsOrderService coinsOrderService;

    @Autowired
    ExecuteService executeService;

    @Autowired
    WorkSpaceService workSpaceService;
    @Autowired
    BinanceExchangeRestServiceImpl binanceExchangeRestService;

    /**
     * U 本位合约
     *
     * @param binanceReceiveMessage
     */
    @EventListener
    public void binanceProcess(FBinanceReceiveMessage binanceReceiveMessage) {
        log.debug("binanceReceiveMessage {}", JSONObject.toJSONString(binanceReceiveMessage));
        //todo 处理 币安 仓位订单数据
        //处理仓位数据
        String msg = binanceReceiveMessage.getMsg();
        JSONObject msgData = JSONObject.parseObject(msg);
        if ("ACCOUNT_UPDATE".equalsIgnoreCase(msgData.getString("e"))) {
            //合约持仓
            JSONObject a = msgData.getJSONObject("a");
            JSONArray positions = a.getJSONArray("P");
            for (Object position : positions) {
                JSONObject from = JSONObject.from(position);
                processSwapPosition(from, Long.valueOf(binanceReceiveMessage.getUserId()), binanceReceiveMessage.getConnectionConfig().getAccount());
            }
            syncProcessSwapPosition(positions, Long.valueOf(binanceReceiveMessage.getUserId()), binanceReceiveMessage.getConnectionConfig().getAccount());
            //处理账户余额
            syncAccount(a.getJSONArray("B"), binanceReceiveMessage.getConnectionConfig().getAccount());

        }

        if ("ORDER_TRADE_UPDATE".equalsIgnoreCase(msgData.getString("e"))) {
            //处理订单数据
            processFOrder(msgData.getJSONObject("o"), binanceReceiveMessage, Long.valueOf(binanceReceiveMessage.getUserId()), binanceReceiveMessage.getConnectionConfig().getAccount());

        }


    }

    /**
     * [                     		// 余额信息
     *         {
     *           "a":"USDT",           		// 资产名称
     *           "wb":"122624.12345678",    	// 钱包余额
     *           "cw":"100.12345678",			// 除去逐仓仓位保证金的钱包余额
     *           "bc":"50.12345678"			// 除去盈亏与交易手续费以外的钱包余额改变量
     *         },
     *         {
     *           "a":"BUSD",
     *           "wb":"1.00000000",
     *           "cw":"0.00000000",
     *           "bc":"-49.12345678"
     *         }
     *       ],
     * @param b
     * @param account
     */
    private void syncAccount(JSONArray b, Account account) {
        for (Object o : b) {
            JSONObject balanceItem = JSONObject.from(o);
            String a = balanceItem.getString("a");
            if ("USDT".equalsIgnoreCase(a)) {
                //处理USDT
                AckAccountEvent ackAccountEvent = new AckAccountEvent();
                ackAccountEvent.setAccount(account);
                ackAccountEvent.setAccountId(account.getId());
                ackAccountEvent.setBalance(balanceItem.getBigDecimal("wb"));
                ackAccountEvent.setFreeBalance(balanceItem.getBigDecimal("cw"));
                SpringUtils.getApplicationContext().publishEvent(ackAccountEvent);

            }
        }


    }

    private void processFOrder(JSONObject binanceOrder, FBinanceReceiveMessage binanceReceiveMessage, Long userId, Account account) {
        /**
         * link https://developers.binance.com/docs/zh-CN/derivatives/usds-margined-futures/user-data-streams/Event-Order-Update
         */
        if (binanceOrder.getString("c").startsWith("autoclose-")) {
            //仓位被强平！
            LiqWarning liqWarning = LiqWarning.fromWsReceiveMsg(binanceReceiveMessage);
            SpringUtils.getApplicationContext().publishEvent(liqWarning);
            return;
        }
        if (binanceOrder.getString("c").startsWith("adl_autoclose")) {
            //仓位被ADL 自动减仓！
            ADLWarning adlWarning = ADLWarning.fromWsReceiveMsg(binanceReceiveMessage);
            SpringUtils.getApplicationContext().publishEvent(adlWarning);
            return;
        }


        CoinsOrderVo coinsOrderVo = coinsOrderService.queryByOrderId(binanceOrder.getString("i"));
        if (Objects.isNull(coinsOrderVo)) {
            log.error("币安 coinsOrderVo is null,order id = {}", binanceOrder.getString("i"));
            return;
        }

        if (Objects.equals(coinsOrderVo.getOrderEnd(), OrderEndStatus.END)) {
            log.warn("订单状态已结束 orderid={}", binanceOrder.getString("i"));

            //可以更新盈亏手续费
            LambdaUpdateWrapper<CoinsOrder> orderUpdate = new LambdaUpdateWrapper<>();
            orderUpdate.eq(CoinsOrder::getId, coinsOrderVo.getId());
            orderUpdate.set(CoinsOrder::getPnl, binanceOrder.getBigDecimal("rp"));
            orderUpdate.set(CoinsOrder::getFee, binanceOrder.getBigDecimal("n"));
            coinsOrderService.getBaseMapper().update(orderUpdate);
            return;
        }
        //中间态订单状态
        List<String> binanceProcessStatus = OrderStatusConstant.binanceProcessStatus;
        List<String> binanceEndStatus = OrderStatusConstant.binanceEndStatus;
        //订单状态
        String state = binanceOrder.getString("X");
        LambdaUpdateWrapper<CoinsOrder> orderUpdate = new LambdaUpdateWrapper<>();
        orderUpdate.eq(CoinsOrder::getId, coinsOrderVo.getId());
        orderUpdate.set(CoinsOrder::getEx, ExchangeType.BINANCE.getName());
        orderUpdate.set(CoinsOrder::getPrice, binanceOrder.getBigDecimal("p"));
        orderUpdate.set(CoinsOrder::getSize, binanceOrder.getBigDecimal("q"));
        orderUpdate.set(CoinsOrder::getStatus, state.toLowerCase());
        //累计成交数量
        orderUpdate.set(CoinsOrder::getCumExecQty, binanceOrder.getBigDecimal("z"));
        orderUpdate.set(CoinsOrder::getAvgPrice, binanceOrder.getBigDecimal("ap"));
        orderUpdate.set(CoinsOrder::getFee, binanceOrder.getBigDecimal("n"));
        if (Objects.nonNull(binanceOrder.getBigDecimal("rp"))) {
            orderUpdate.set(CoinsOrder::getPnl, binanceOrder.getBigDecimal("rp"));
        }

        boolean end = false;
        if (StringListUtil.containsIgnoreCase(binanceProcessStatus, state)) {
            //中间态
            orderUpdate.set(CoinsOrder::getOrderEnd, OrderEndStatus.NOT_END);
        } else if (StringListUtil.containsIgnoreCase(binanceEndStatus, state)) {
            //结束态
            orderUpdate.set(CoinsOrder::getOrderEnd, OrderEndStatus.END);
            if (Objects.nonNull(binanceOrder.getBigDecimal("ap"))) {
                orderUpdate.set(CoinsOrder::getCumExecValue, binanceOrder.getBigDecimal("z").multiply(binanceOrder.getBigDecimal("ap")));
            }
            end = true;

        }
        coinsOrderService.getBaseMapper().update(orderUpdate);
        if (coinsOrderVo.getBatchId() != null && end) {
            //是批次单并且结束了
            SpringUtils.getBean(BatchOrderTaskService.class).checkAndRunBatchOrderTask();
        }
        workSpaceService.sendOrderWebsocket(coinsOrderVo.getId());

    }

    private void syncProcessSwapPosition(JSONArray data, Long userId, Account account) {
//        List<PositionWsData> wsData = new ArrayList<>();
//        for (Object position : data) {
//            JSONObject fbPositionData = JSONObject.from(position);
//            String s = fbPositionData.getString("s");
//            String symbol = s.replaceAll("USDT", "");
//            List<PositionWsData> positionWsData = binanceExchangeRestService.queryContractPositionDetail(account, new PositionParams());
//
//
//
//        }
        //发送持仓状态给前端
        List<PositionWsData> positionWsData = binanceExchangeRestService.queryContractPositionDetail(account, new PositionParams());
//        for (PositionWsData positionWsDatum : positionWsData) {
        PositionWebsocketMsgData<List<PositionWsData>> websocketMsgData = new PositionWebsocketMsgData<>();
        websocketMsgData.setType(WebsocketMsgType.POSITION);
        websocketMsgData.setData(positionWsData);
        websocketMsgData.setAccountId(account.getId());
        websocketMsgData.setUserId(account.getUserId());
        websocketMsgData.setExchangeName(ExchangeType.BINANCE.getName());
        SpringUtils.getApplicationContext().publishEvent(websocketMsgData);
//        }


    }

    private void processSwapPosition(JSONObject fbPositionData, Long userId, Account account) {
        String s = fbPositionData.getString("s");
        String symbol = s.replaceAll("USDT", "");


        CoinsCrossExchangeArbitrageTaskVo taskVo = coinsCrossExchangeArbitrageTaskService.queryActiveTaskByUserIdSymbolAndAccountId(symbol, userId, account.getId());
        if (Objects.isNull(taskVo)) {
            log.info("未查找到对应的套利任务 userId:{} ex:{} symbol:{}", userId, ExchangeType.BINANCE.getName(), symbol);
            return;
        }
        if (taskVo.getLongEx().equalsIgnoreCase(ExchangeType.BINANCE.getName()) && Objects.equals(taskVo.getLongAccountId(), account.getId())) {
            //做多方
            if (fbPositionData.getString("ps").equalsIgnoreCase(SideType.LONG)
                || fbPositionData.getBigDecimal("pa").compareTo(BigDecimal.ZERO) >= 0) {
                UpdateWrapper<CoinsCrossExchangeArbitrageTask> taskUpdateWrapper = new UpdateWrapper<>();
                LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask> lambda = taskUpdateWrapper.lambda();
                lambda.eq(CoinsCrossExchangeArbitrageTask::getId, taskVo.getId());
                lambda.set(CoinsCrossExchangeArbitrageTask::getLongSymbolSize, fbPositionData.getBigDecimal("pa"));
                lambda.set(CoinsCrossExchangeArbitrageTask::getLongAvgPrice, fbPositionData.getBigDecimal("ep"));
                lambda.set(CoinsCrossExchangeArbitrageTask::getLongFundingFee, BigDecimal.ZERO);
                lambda.set(CoinsCrossExchangeArbitrageTask::getLongProfit, fbPositionData.getBigDecimal("up"));
                coinsCrossExchangeArbitrageTaskService.getBaseMapper().update(lambda);

            } else {
                log.error("币安 币种 {} 持仓方向与任务方向不一致 持仓方向{}  任务方向 做多", symbol, fbPositionData.getString("ps"));
            }

        } else if (taskVo.getShortEx().equalsIgnoreCase(ExchangeType.BINANCE.getName()) && Objects.equals(taskVo.getShortAccountId(), account.getId())) {
            //做空方
            if (fbPositionData.getString("ps").equalsIgnoreCase(SideType.SHORT)
                || fbPositionData.getBigDecimal("pa").compareTo(BigDecimal.ZERO) <= 0) {
                UpdateWrapper<CoinsCrossExchangeArbitrageTask> taskUpdateWrapper = new UpdateWrapper<>();
                LambdaUpdateWrapper<CoinsCrossExchangeArbitrageTask> lambda = taskUpdateWrapper.lambda();
                lambda.eq(CoinsCrossExchangeArbitrageTask::getId, taskVo.getId());
                lambda.set(CoinsCrossExchangeArbitrageTask::getShortSymbolSize, fbPositionData.getBigDecimal("pa"));
                lambda.set(CoinsCrossExchangeArbitrageTask::getShortAvgPrice, fbPositionData.getBigDecimal("ep"));
                lambda.set(CoinsCrossExchangeArbitrageTask::getShortFundingFee, BigDecimal.ZERO);
                lambda.set(CoinsCrossExchangeArbitrageTask::getShortProfit, fbPositionData.getBigDecimal("up"));
                coinsCrossExchangeArbitrageTaskService.getBaseMapper().update(lambda);
            } else {
                log.error("币安 币种 {} 持仓方向与任务方向不一致 持仓方向{}  任务方向 做空", symbol, fbPositionData.getString("ps"));
            }
        }


    }


}
