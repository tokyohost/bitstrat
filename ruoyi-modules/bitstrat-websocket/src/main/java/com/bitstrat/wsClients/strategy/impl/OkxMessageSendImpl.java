package com.bitstrat.wsClients.strategy.impl;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.wsClients.constant.SocketConstant;
import com.bitstrat.wsClients.strategy.MessageSendStrategy;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
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
public class OkxMessageSendImpl implements MessageSendStrategy {

    @Override
    public String getExchange() {
        return ExchangeType.OKX.getName();
    }

    @Override
    public void sendMessage(String userId, String exchange, String message, Channel channel) {
        channel.writeAndFlush(new TextWebSocketFrame(message));
    }

    @Override
    public void sendSubscriptMessage(String userId, String exchange, String message, Channel channel,String subscriptType) {
        synchronized (channel) {
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
                        log.info("okx 订阅 = {}", message);
                        channel.writeAndFlush(new TextWebSocketFrame(sub));
                    }
                    subscriptQueue.clear();
                }else{
                    log.info("有鉴权要求，鉴权不通过");
                }
            }else{
                log.info("无鉴权要求，发送订阅");
                for (String sub : subscriptQueue) {
                log.info("okx 订阅 = {}", message);
                    channel.writeAndFlush(new TextWebSocketFrame(sub));
                }
            }
        }

    }
}
