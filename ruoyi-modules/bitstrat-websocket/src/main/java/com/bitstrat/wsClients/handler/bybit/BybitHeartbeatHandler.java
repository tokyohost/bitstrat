package com.bitstrat.wsClients.handler.bybit;

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
public class BybitHeartbeatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    //{"req_id": "100001", "op": "ping"}
    private static final String HEARTBEAT_MESSAGE = "{\"op\":\"ping\"}";

    ConnectionConfig connectionConfig;

    public BybitHeartbeatHandler(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 在连接激活后启动定时器发送心跳消息
        ScheduledFuture<?> scheduledFuture = ctx.executor().scheduleAtFixedRate(() -> {
            ctx.channel().writeAndFlush(new TextWebSocketFrame(HEARTBEAT_MESSAGE));
            connectionConfig.setLastPingTimeStamp(System.currentTimeMillis());
            log.info("userid {} exchange{} websocketType {} heatbeat send", connectionConfig.getUserId(), connectionConfig.getExchange(),
                connectionConfig.getWebsocketType());
        }, 0, 20, TimeUnit.SECONDS);// 每 30 秒发送一次心跳
        connectionConfig.setHeatebeatScheduledFuture(scheduledFuture);
        ctx.fireChannelActive();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        String pongMsg = msg.text();
        String pongText = "\"op\":\"pong\"";
        String pingText = "\"op\":\"ping\"";
        if(pongMsg.contains(pongText) || pongMsg.contains(pingText)) {
            log.info(msg.text());
            log.info("[{} {}]Received pong from Bybit",connectionConfig.getUserId(),connectionConfig.getExchange());
            connectionConfig.setLastPongTimeStamp(System.currentTimeMillis());
            connectionConfig.setDely(connectionConfig.getLastPongTimeStamp() - connectionConfig.getLastPingTimeStamp());
            log.info("bybit dely time:{}",connectionConfig.getDely());
            return;
        }

        ctx.fireChannelRead(msg.retain());
    }
}
