package com.bitstrat.wsClients.handler.bitget;

import com.bitstrat.wsClients.domian.ConnectionConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
@Slf4j
public class BitgetHeartbeatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final String HEARTBEAT_MESSAGE = "ping";
    ConnectionConfig connectionConfig;

    public BitgetHeartbeatHandler(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 每 30 秒发送心跳
        ScheduledFuture<?> scheduledFuture = ctx.executor().scheduleAtFixedRate(() -> {
            ctx.channel().writeAndFlush(new TextWebSocketFrame(HEARTBEAT_MESSAGE));
            connectionConfig.setLastPingTimeStamp(System.currentTimeMillis());
        }, 0, 30, TimeUnit.SECONDS);// 每 30 秒发送一次心跳
        connectionConfig.setHeatebeatScheduledFuture(scheduledFuture);
        ctx.fireChannelActive();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        if (msg.text().contains("pong")) {
            log.info("[{} {}]Received pong from Bitget",connectionConfig.getUserId(), connectionConfig.getExchange());
            connectionConfig.setLastPongTimeStamp(System.currentTimeMillis());
            connectionConfig.setDely(connectionConfig.getLastPongTimeStamp() - connectionConfig.getLastPingTimeStamp());
            log.info("Bitget dely time:{}",connectionConfig.getDely());
            return;
        }
        ctx.fireChannelRead(msg.retain());
    }
}
