package com.bitstrat.wsClients.handler.okx;

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
public class OkxHeartbeatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final String HEARTBEAT_MESSAGE = "ping";

    ConnectionConfig connectionConfig;

    public OkxHeartbeatHandler(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 启动定时发送心跳消息
        ScheduledFuture<?> scheduledFuture = ctx.executor().scheduleAtFixedRate(() -> {
            ctx.channel().writeAndFlush(new TextWebSocketFrame(HEARTBEAT_MESSAGE));
            connectionConfig.setLastPingTimeStamp(System.currentTimeMillis());
        }, 0, 10, TimeUnit.SECONDS);// 每 10 秒发送一次心跳
        connectionConfig.setHeatebeatScheduledFuture(scheduledFuture);
        ctx.fireChannelActive();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        if (msg.text().contains("pong")) {
            log.info("[{} {}]Received pong from OKX",connectionConfig.getUserId(), connectionConfig.getExchange());
            connectionConfig.setLastPongTimeStamp(System.currentTimeMillis());
            connectionConfig.setDely(connectionConfig.getLastPongTimeStamp() - connectionConfig.getLastPingTimeStamp());
            log.info("okx dely time:{}",connectionConfig.getDely());
            return;
        }
        ctx.fireChannelRead(msg.retain());
    }
}
