package com.bitstrat.service;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.WebsocketMsgType;
import com.bitstrat.domain.WebsocketExStatus;
import com.bitstrat.domain.WebsocketMsgData;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.domain.vo.CoinsOrderVo;
import com.bitstrat.domain.vo.WebsocketStatus;
import com.bitstrat.store.ExecuteService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.websocket.dto.AccountAutoSend;
import org.dromara.common.websocket.utils.WebSocketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WorkSpaceService {

    @Autowired
    ICoinsOrderService coinsOrderService;
    @Autowired
    ExecuteService executeService;

    @Autowired
    ICoinsApiService coinsApiService;

    public void sendOrderWebsocket(Long orderId) {
        CoinsOrderVo coinsOrderVo = coinsOrderService.queryById(orderId);
        executeService.getWebsocketNotifyExecute().submit(()->{
            WebsocketMsgData<CoinsOrderVo> websocketMsgData = new WebsocketMsgData<>();
            websocketMsgData.setData(coinsOrderVo);
            websocketMsgData.setType(WebsocketMsgType.ORDER);
            WebSocketUtils.sendMessage(coinsOrderVo.getCreateBy(),websocketMsgData.toJSONString());
            log.info("已发送订单状态至前端 {}",websocketMsgData.toJSONString());
        });
    }

    @Async
    @EventListener(AccountAutoSend.class)
    public void accountAutoSend(AccountAutoSend autoSend) {
        List<WebsocketExStatus> result = coinsApiService.getWebsocketExStatuses(autoSend.getUserId());
        Set<Long> apiIds = new HashSet<>();
        for (WebsocketExStatus websocketExStatus : result) {
            Set<Long> ids = websocketExStatus.getDatas().stream().map(WebsocketStatus::getApiId).collect(Collectors.toSet());
            apiIds.addAll(ids);
        }
        List<CoinsApiVo> apiVos = coinsApiService.queryByIds(new ArrayList<>(apiIds));
        Map<Long, CoinsApiVo> apiVoMap = apiVos.stream().collect(Collectors.toMap(CoinsApiVo::getId, item -> item, (a, b) -> a));

        for (WebsocketExStatus websocketExStatus : result) {
            for (WebsocketStatus data : websocketExStatus.getDatas()) {
                CoinsApiVo coinsApiVo = apiVoMap.get(data.getApiId());
                data.setBalance(coinsApiVo.getBalance());
                data.setFreeBalance(coinsApiVo.getFreeBalance());
                data.setUpdateTime(coinsApiVo.getBalanceUpdate());
            }
        }

        WebsocketMsgData<List<WebsocketExStatus>> msgData = new WebsocketMsgData<>();
        msgData.setData(result);
        msgData.setType(WebsocketMsgType.ACCOUNT);

        WebSocketUtils.sendMessage(autoSend.getUserId(), msgData.toJSONString());
//        log.info("account auto send");
    }
}
