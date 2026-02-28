package com.bitstrat.wsClients;


import com.bitstrat.constant.ExchangeType;
import com.bitstrat.wsClients.domian.ConnectionConfig;
import com.bitstrat.wsClients.handler.*;
import com.bitstrat.wsClients.handler.binance.BinanceAuthHandler;
import com.bitstrat.wsClients.handler.binance.BinanceDelyTestHandler;
import com.bitstrat.wsClients.handler.binance.BinanceHeartbeatHandler;
import com.bitstrat.wsClients.handler.binance.BinanceSocketFrameHandler;
import com.bitstrat.wsClients.handler.bitget.BitgetAuthHandler;
import com.bitstrat.wsClients.handler.bitget.BitgetHeartbeatHandler;
import com.bitstrat.wsClients.handler.bitget.BitgetSocketFrameHandler;
import com.bitstrat.wsClients.handler.bybit.BybitAuthHandler;
import com.bitstrat.wsClients.handler.bybit.BybitHeartbeatHandler;
import com.bitstrat.wsClients.handler.bybit.BybitSocketFrameHandler;
import com.bitstrat.wsClients.handler.okx.OkxAuthHandler;
import com.bitstrat.wsClients.handler.okx.OkxHeartbeatHandler;
import com.bitstrat.wsClients.handler.okx.OkxSocketFrameHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.IdleStateHandler;

import javax.net.ssl.SSLException;
import java.util.concurrent.TimeUnit;

import static com.bitstrat.wsClients.constant.SocketConstant.RECONNECT_HANDLER_ATTRIBUTE_KEY;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/27 11:12
 * @Content
 */

public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final ConnectionConfig connectionConfig;
    public WebSocketChannelInitializer(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }
    @Override
    protected void initChannel(SocketChannel ch) throws SSLException {
        ChannelPipeline pipeline = ch.pipeline();

        String scheme = connectionConfig.getUri().getScheme();
        String host = connectionConfig.getUri().getHost();
        int port = connectionConfig.getUri().getPort();
        if (port == -1) {
            port = "wss".equalsIgnoreCase(scheme) ? 443 : 80;
        }
        boolean isSsl = "wss".equalsIgnoreCase(connectionConfig.getUri().getScheme());


        if (isSsl) {
            //SSL handler
            SslContext sslCtx = SslContextBuilder.forClient().build();
            pipeline.addLast(sslCtx.newHandler(ch.alloc(), host, port));
        }
        // HTTP编解码
        pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new HttpObjectAggregator(8192));
        // WebSocket握手处理
        HttpHeaders headers = new DefaultHttpHeaders();
        // 创建 WebSocket 客户端握手处理器
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
            connectionConfig.getUri(),
            WebSocketVersion.V13,
            null, true, headers);
        connectionConfig.setWebSocketClientHandshaker(handshaker);
        pipeline.addLast(new WebSocketClientProtocolHandler(handshaker));



        // 添加 WebSocket 数据处理器
        WebSocketReconnectHandler reconnectHandler = new WebSocketReconnectHandler(connectionConfig);
        ch.attr(RECONNECT_HANDLER_ATTRIBUTE_KEY).set(reconnectHandler);
        pipeline.addLast(reconnectHandler);

        // 根据交易所选择不同的鉴权和心跳机制
        if (ExchangeType.BYBIT.getName().equalsIgnoreCase(connectionConfig.getExchange())) {
            pipeline.addLast(new BybitAuthHandler(connectionConfig));
            // 加心跳检测，超时触发事件
            pipeline.addLast(new IdleStateHandler(60, 60, 0, TimeUnit.SECONDS));
            pipeline.addLast(new BybitHeartbeatHandler(connectionConfig));

            //主要预处理Liner数据
            pipeline.addLast(new LinerSocketFrameHandler(connectionConfig));
            pipeline.addLast(new BybitSocketFrameHandler(connectionConfig));
        } else if (ExchangeType.OKX.getName().equalsIgnoreCase(connectionConfig.getExchange())) {
            pipeline.addLast(new OkxAuthHandler(connectionConfig));
            // 加心跳检测，超时触发事件
            pipeline.addLast(new IdleStateHandler(60, 60, 0, TimeUnit.SECONDS));

            pipeline.addLast(new OkxHeartbeatHandler(connectionConfig));

            //主要预处理Liner数据
            pipeline.addLast(new LinerSocketFrameHandler(connectionConfig));
            pipeline.addLast(new OkxSocketFrameHandler(connectionConfig));
        } else if (ExchangeType.BITGET.getName().equalsIgnoreCase(connectionConfig.getExchange())) {
            pipeline.addLast(new BitgetAuthHandler(connectionConfig));
            // 加心跳检测，超时触发事件
            pipeline.addLast(new IdleStateHandler(60, 60, 0, TimeUnit.SECONDS));

            pipeline.addLast(new BitgetHeartbeatHandler(connectionConfig));

            //主要预处理Liner数据
            pipeline.addLast(new LinerSocketFrameHandler(connectionConfig));
            pipeline.addLast(new BitgetSocketFrameHandler(connectionConfig));
        } else if (ExchangeType.BINANCE.getName().equalsIgnoreCase(connectionConfig.getExchange())) {
            pipeline.addLast(new BinanceAuthHandler(connectionConfig));
            // 加心跳检测，超时触发事件
            pipeline.addLast(new IdleStateHandler(60, 60, 0, TimeUnit.SECONDS));
            pipeline.addLast(new BinanceDelyTestHandler(connectionConfig));

            pipeline.addLast(new BinanceHeartbeatHandler(connectionConfig));

            //主要预处理Liner数据
            pipeline.addLast(new LinerSocketFrameHandler(connectionConfig));
            pipeline.addLast(new BinanceSocketFrameHandler(connectionConfig));
        }




        pipeline.addLast(new WebSocketFrameHandler(connectionConfig));

    }
}
