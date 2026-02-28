package com.bitstrat.wsServer;

import com.bitstrat.config.UserNettyConfig;
import com.bitstrat.wsServer.handler.UserWsHttpRequestHandler;
import com.bitstrat.wsServer.handler.WebSocketHandlerUser;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;


/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 15:00
 * @Content
 */

public class WebSocketUserServerInitializer extends ChannelInitializer<SocketChannel> {
    UserNettyConfig nettyConfig;
    WebSocketHandlerUser webSocketHandler;
    UserWsHttpRequestHandler httpRequestHandler;
    public WebSocketUserServerInitializer(UserNettyConfig config,
                                          WebSocketHandlerUser webSocketHandler,
                                          UserWsHttpRequestHandler httpRequestHandler) {
        this.nettyConfig = config;
        this.webSocketHandler = webSocketHandler;
        this.httpRequestHandler = httpRequestHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        WebSocketServerProtocolConfig config = WebSocketServerProtocolConfig.newBuilder()
            .websocketPath(nettyConfig.getPath())
            .checkStartsWith(true) // 支持带参数访问（比如 /server?token=123）
            .allowExtensions(true)
            .maxFramePayloadLength(65536)
            .build();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536)); // 聚合成 FullHttpRequest
        pipeline.addLast(httpRequestHandler); // 聚合成 FullHttpRequest
        pipeline.addLast(new WebSocketServerProtocolHandler(config)); // WebSocket 路径
        pipeline.addLast(new LoggingHandler());
        pipeline.addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast(webSocketHandler);
    }
}
