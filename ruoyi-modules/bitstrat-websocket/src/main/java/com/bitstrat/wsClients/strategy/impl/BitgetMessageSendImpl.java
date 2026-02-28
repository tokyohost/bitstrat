package com.bitstrat.wsClients.strategy.impl;

import cn.hutool.core.collection.ConcurrentHashSet;
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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/27 16:59
 * @Content
 */

@Slf4j
@Component
public class BitgetMessageSendImpl implements MessageSendStrategy {

    @Override
    public String getExchange() {
        return ExchangeType.BITGET.getName();
    }

    @Override
    public void sendMessage(String userId, String exchange, String message, Channel channel) {
        channel.writeAndFlush(new TextWebSocketFrame(message));
    }

    @Override
    public void sendSubscriptMessage(String userId, String exchange, String message, Channel channel,String subscriptType) {
        ConcurrentHashSet<String> subscriptQueue = channel.attr(SocketConstant.subscriptQueue).get();
        if(Objects.nonNull(subscriptQueue)){
            synchronized (subscriptQueue){
                subscriptQueue.add(message);
                channel.attr(SocketConstant.subscriptQueue).set(subscriptQueue);
            }
        }else{
            subscriptQueue = new ConcurrentHashSet<>();
            subscriptQueue.add(message);
            channel.attr(SocketConstant.subscriptQueue).set(subscriptQueue);
        }
        Boolean requireAuth = channel.attr(SocketConstant.requiresAuthentication).get();
        if(Objects.nonNull(requireAuth) && requireAuth){
            Boolean b = channel.attr(SocketConstant.IS_AUTHENTICATED).get();
            if (b != null && b) {
                log.info("有鉴权要求，鉴权通过，发送订阅");
                for (String sub : subscriptQueue) {
                    log.info("biteget 订阅 = {}", sub);
                    channel.writeAndFlush(new TextWebSocketFrame(sub));
                }
                subscriptQueue.clear();
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
}
