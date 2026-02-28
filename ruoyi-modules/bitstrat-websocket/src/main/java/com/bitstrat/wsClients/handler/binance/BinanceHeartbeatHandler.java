package com.bitstrat.wsClients.handler.binance;

import com.bitstrat.wsClients.domian.ConnectionConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
@ChannelHandler.Sharable
public class BinanceHeartbeatHandler extends SimpleChannelInboundHandler<PingWebSocketFrame> {
    ConnectionConfig connectionConfig;

    public BinanceHeartbeatHandler(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    private static final String HEARTBEAT_MESSAGE = "{\"method\": \"PING\"}";

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
//        // 每 20分钟续约
        ScheduledFuture<?> scheduledFuture = ctx.executor().scheduleAtFixedRate(() -> {
            BinanceAuthUtils.extendUMstreamUrl(connectionConfig.getAccount());
        }, 0, 2, TimeUnit.MINUTES);// 每 30 秒发送一次心跳
        connectionConfig.setHeatebeatScheduledFuture(scheduledFuture);
        ctx.fireChannelActive();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PingWebSocketFrame msg) {
        log.info("receive ping websocket frame from 币安 userId :{}", connectionConfig.getUserId());
        //立刻回复pong
        // 收到 Ping，复制内容
        ByteBuf pingData = msg.content().retain();
        // 回复 Pong
        ctx.channel().writeAndFlush(new PongWebSocketFrame(pingData));
        ctx.fireChannelRead(msg.retain());
    }
}
