package com.bitstrat.wsClients.strategy.impl;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.wsClients.constant.SocketConstant;
import com.bitstrat.wsClients.constant.SubscriptMsgType;
import com.bitstrat.wsClients.domian.ConnectionConfig;
import com.bitstrat.wsClients.strategy.MessageSendStrategy;
import com.bitstrat.wsClients.utils.AckPositionUtils;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/27 16:59
 * @Content
 */

@Slf4j
@Component
public class BybitMessageSendImpl implements MessageSendStrategy {

    @Override
    public String getExchange() {
        return ExchangeType.BYBIT.getName();
    }

    @Override
    public void sendMessage(String userId, String exchange, String message, Channel channel) {
        Boolean requireAuth = channel.attr(SocketConstant.requiresAuthentication).get();
        if(Objects.nonNull(requireAuth) && requireAuth){
            Boolean b = channel.attr(SocketConstant.IS_AUTHENTICATED).get();
            if (b != null && b) {
                channel.writeAndFlush(new TextWebSocketFrame(message));
            }else{
                //bybit 如果鉴权没通过，消息先存队列里
                List<String> messageQueue = channel.attr(SocketConstant.ByBitMsgQueue).setIfAbsent(new ArrayList<>());
                if(Objects.isNull(messageQueue)){
                    messageQueue = new ArrayList<>();
                }
                messageQueue.add(message);
                channel.attr(SocketConstant.ByBitMsgQueue).set(messageQueue);
            }
        }else{
            channel.writeAndFlush(new TextWebSocketFrame(message));
        }
    }

    @Override
    public void sendSubscriptMessage(String userId, String exchange, String message, Channel channel,String subscriptType) {
        ConcurrentHashSet<String> subscriptQueue = channel.attr(SocketConstant.subscriptQueue).get();
        if(Objects.nonNull(subscriptQueue)){
            synchronized (subscriptQueue){
                subscriptQueue.add(message);
                subscriptQueue = deDuplication(subscriptQueue);
                channel.attr(SocketConstant.subscriptQueue).set(subscriptQueue);
            }
        }else{
            subscriptQueue = new ConcurrentHashSet<>();
            subscriptQueue.add(message);
            subscriptQueue = deDuplication(subscriptQueue);
            channel.attr(SocketConstant.subscriptQueue).set(subscriptQueue);
        }
        Boolean requireAuth = channel.attr(SocketConstant.requiresAuthentication).get();
        if(Objects.nonNull(requireAuth) && requireAuth){
            Boolean b = channel.attr(SocketConstant.IS_AUTHENTICATED).get();
            if (b != null && b) {
                log.info("有鉴权要求，鉴权通过，发送订阅");
                for (String sub : subscriptQueue) {
                    log.info("bybit 订阅 = {}", sub);
                    channel.writeAndFlush(new TextWebSocketFrame(sub));
                }
            }else{
                log.info("有鉴权要求，鉴权不通过");
            }
        }else{
            log.info("无鉴权要求，发送订阅");
            for (String sub : subscriptQueue) {
                channel.writeAndFlush(new TextWebSocketFrame(sub));
            }
        }

        if(SubscriptMsgType.POSITION.equalsIgnoreCase(subscriptType)){
            ConnectionConfig connectionConfig = channel.attr(SocketConstant.connectionConfig).get();
            NioEventLoopGroup loopGroup = connectionConfig.getLoopGroup();
            ScheduledFuture<?> positionSyncScheduledFuture = connectionConfig.getPositionSyncScheduledFuture();

            if(Objects.isNull(positionSyncScheduledFuture)){
                ScheduledFuture<?> scheduledFuture = AckPositionUtils.startAckPosition(loopGroup, connectionConfig, 5L, 15L);
                connectionConfig.setPositionSyncScheduledFuture(scheduledFuture);
            }
        }

    }

    private ConcurrentHashSet<String> deDuplication(ConcurrentHashSet<String> subscriptQueue) {
        //{"op":"subscribe","req_id":"65023df5-7532-4caf-a92a-500d2ea52460","args":["execution"]}
        //去掉req_id
        HashMap<String, String> duMap = new HashMap<>();
        for (String s : subscriptQueue) {
            JSONObject parsed = JSONObject.parseObject(s);
            parsed.remove("req_id");
            String key = parsed.toJSONString();
            duMap.put(key, s);
        }
        return new ConcurrentHashSet<>(duMap.values());
    }
}
