package com.bitstrat.task;

import com.bitstrat.config.ExchangeWebsocketProperties;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;
import com.bitstrat.service.ICoinsApiService;
import com.bitstrat.service.ICoinsOrderService;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.APITypeHelper;
import com.bitstrat.utils.AccountUtils;
import com.bitstrat.wsClients.ExchangeConnectionManager;
import com.bitstrat.wsClients.constant.SubscriptMsgType;
import com.bitstrat.wsClients.constant.WebSocketType;
import com.bitstrat.wsClients.msg.SubscriptMsgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TaskServiceImpl implements TaskService {
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
    @Override
    @Deprecated
    public void startWsSocket(List<CoinsCrossExchangeArbitrageTaskVo> coinsCrossExchangeArbitrageTaskVos) {
        Map<Long, List<CoinsCrossExchangeArbitrageTaskVo>> userTaskGroup = coinsCrossExchangeArbitrageTaskVos.stream().collect(Collectors.groupingBy(CoinsCrossExchangeArbitrageTaskVo::getUserId));
        Set<Long> allAccountId = coinsCrossExchangeArbitrageTaskVos.stream().map(CoinsCrossExchangeArbitrageTaskVo::getLongAccountId).collect(Collectors.toSet());
        allAccountId.addAll(coinsCrossExchangeArbitrageTaskVos.stream().map(CoinsCrossExchangeArbitrageTaskVo::getShortAccountId).collect(Collectors.toSet()));

        List<CoinsApiVo> allAccountList = coinsApiService.queryByIds(new ArrayList<>(allAccountId));
        Map<Long, List<CoinsApiVo>> userAccountMap = allAccountList.stream().collect(Collectors.groupingBy(CoinsApiVo::getUserId));
        for (Long userId : userTaskGroup.keySet()) {
            List<CoinsCrossExchangeArbitrageTaskVo> groupByUser = userTaskGroup.getOrDefault(userId, new ArrayList<>());
            Set<String> longEx = groupByUser.stream().map(CoinsCrossExchangeArbitrageTaskVo::getLongEx).collect(Collectors.toSet());
            Set<String> shortEx = groupByUser.stream().map(CoinsCrossExchangeArbitrageTaskVo::getShortEx).collect(Collectors.toSet());
            longEx.addAll(shortEx);
            List<CoinsApiVo> accounts = userAccountMap.get(userId);
            Map<String, List<CoinsApiVo>> exAccountMap = accounts.stream().collect(Collectors.groupingBy((item) -> item.getExchangeName().toLowerCase()));
            for (String exlong : longEx) {
                String ex = exlong.toLowerCase();
                List<CoinsApiVo> coinsApiVos = exAccountMap.get(ex);
                if(CollectionUtils.isEmpty(coinsApiVos)){
                    continue;
                }
                for (CoinsApiVo coinsApiVo : coinsApiVos) {
                    if(coinsApiVo != null) {
                        Account account = AccountUtils.coverToAccount(coinsApiVo);
                        APITypeHelper.set(coinsApiVo.getType());
                        try {
                            String urlByExAndType = exchangeWebsocketProperties.getUrlByExAndType(ex, WebSocketType.PRIVATE);
                            if(StringUtils.isEmpty(urlByExAndType)) {
                                log.error("urlByExAndType is empty userid{} ex {} type: {}",userId, ex, WebSocketType.PRIVATE);
                                continue;
                            }
                            try {
                                ExchangeService exchangeService = exchangeApiManager.getExchangeService(ex);
                                SubscriptMsgs wsSubscriptMsgs = exchangeService.getWsSubscriptMsgs();
                                exchangeConnectionManager.createConnection(account,userId+"",ex,WebSocketType.PRIVATE, URI.create(urlByExAndType),null,false,(channel) ->{
                                    //监听仓位变动
                                    exchangeConnectionManager.sendSubscriptMessage(userId+"",account.getId(),ex,WebSocketType.PRIVATE,wsSubscriptMsgs.createSubscriptPositionMsg(), SubscriptMsgType.POSITION,channel);
                                    //监听订单成交
                                    exchangeConnectionManager.sendSubscriptMessage(userId+"",account.getId(),ex,WebSocketType.PRIVATE,wsSubscriptMsgs.createSubscriptOrderMsg(),null,channel);
                                    //监听余额变动
                                    exchangeConnectionManager.sendSubscriptMessage(userId+"",account.getId(),ex,WebSocketType.PRIVATE,wsSubscriptMsgs.createSubscriptAccountMsg(),null,channel);
                                });


                            } catch (Exception e) {
                                log.error("ws 监听仓位失败 {} userid {} ex {} type {} url:{}",e.getMessage(),userId,ex,WebSocketType.PRIVATE,urlByExAndType,e);
                            }
                        }finally {
                            APITypeHelper.clear();
                        }
                    }
                }
            }
        }

    }
}
