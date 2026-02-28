package com.bitstrat.server;

import com.bitstrat.config.NettyConfig;
import com.bitstrat.server.handler.WebSocketHandlerBitstrat;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;


/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 15:00
 * @Content
 */

@Deprecated
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
    NettyConfig nettyConfig;
    WebSocketHandlerBitstrat webSocketHandler;
    public WebSocketServerInitializer(NettyConfig config, WebSocketHandlerBitstrat webSocketHandler) {
        this.nettyConfig = config;
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536)); // 聚合成 FullHttpRequest
        pipeline.addLast(new WebSocketServerProtocolHandler("/server")); // WebSocket 路径
        pipeline.addLast(new IdleStateHandler(0, 0, nettyConfig.getConnectionTimeout(), TimeUnit.SECONDS));
        pipeline.addLast(webSocketHandler);
    }
}
