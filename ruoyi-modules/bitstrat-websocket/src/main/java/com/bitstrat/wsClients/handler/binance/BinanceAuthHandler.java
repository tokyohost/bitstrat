package com.bitstrat.wsClients.handler.binance;

import com.bitstrat.wsClients.constant.SocketConstant;
import com.bitstrat.wsClients.domian.ConnectionConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/27 11:23
 * @Content
 */
@Slf4j
@ChannelHandler.Sharable
public class BinanceAuthHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    ConnectionConfig connectionConfig;

    public BinanceAuthHandler(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.channel().attr(SocketConstant.connectionConfig).setIfAbsent(connectionConfig);
        connectionConfig.getAwaitConnectDone().countDown();
        log.info("币安 websocket 链接创建成功 userId:{}", connectionConfig.getUserId());
        ctx.fireChannelActive();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        ctx.fireChannelRead(msg.retain());
    }
}

