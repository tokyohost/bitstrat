package com.bitstrat.wsClients.strategy.impl;

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
import java.util.concurrent.TimeUnit;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/27 16:59
 * @Content
 */

@Slf4j
@Component
public class BinanceMessageSendImpl implements MessageSendStrategy {


    @Override
    public String getExchange() {
        return ExchangeType.BINANCE.getName();
    }

    @Override
    public void sendMessage(String userId, String exchange, String message, Channel channel) {
        channel.writeAndFlush(new TextWebSocketFrame(message));
    }

    @Override
    public void sendSubscriptMessage(String userId, String exchange, String message, Channel channel,String subscriptType) {
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
