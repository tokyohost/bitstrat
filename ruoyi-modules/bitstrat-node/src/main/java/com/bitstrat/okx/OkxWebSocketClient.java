package com.bitstrat.okx;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.bitstrat.cache.MarketPriceCache;
import com.bitstrat.okx.handle.OkxMessageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@ChannelHandler.Sharable
@Slf4j
public class OkxWebSocketClient {

    @Value("${okx.ws-url}")
    String url = "wss://ws.okx.com:8443/ws/v5/public";
    MarketPriceCache marketPriceCache;
    OkxMessageHandler messageHandler;
    ConcurrentHashSet<String> swapSymbol = new ConcurrentHashSet<>();
    ConcurrentHashSet<String> spotSymbol = new ConcurrentHashSet<>();

    public ConcurrentHashSet<String> getSwapSymbol() {
        return swapSymbol;
    }

    public ConcurrentHashSet<String> getSpotSymbol() {
        return spotSymbol;
    }

    private final EventLoopGroup group = new NioEventLoopGroup();
    private final WebSocketHandler handler ;

    private Channel channel;

    public OkxWebSocketClient(OkxMessageHandler messageHandler,MarketPriceCache marketPriceCache) {
        this.messageHandler = messageHandler;
        this.marketPriceCache = marketPriceCache;
        this.handler = new WebSocketHandler(this,marketPriceCache,messageHandler);
        this.connect();

    }

    public void connect() {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();

            final SslContext sslCtx = SslContextBuilder.forClient().build();
            HttpHeaders headers = new DefaultHttpHeaders();
            WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, true, headers);
            handler.setHandshaker(handshaker);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                        p.addLast(new HttpClientCodec());
                        p.addLast(new HttpObjectAggregator(8192));
                        p.addLast(new IdleStateHandler(20, 10, 0, TimeUnit.SECONDS));
                        p.addLast(handler);
                    }
                });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            this.channel = future.channel();

            String path = uri.getRawPath();

            handshaker.handshake(channel);
        } catch (Exception e) {
            reconnect();
        }
    }

    public void reconnect() {
        log.warn("okx websocket 尝试重连...");
        group.schedule(this::connect, 5, TimeUnit.SECONDS);
    }

    public void send(String msg) {
        if (channel != null && channel.isOpen()) {
            channel.writeAndFlush(new TextWebSocketFrame(msg));
        }
    }
    @PreDestroy
    public void shutdown() {
        group.shutdownGracefully();
    }
}
