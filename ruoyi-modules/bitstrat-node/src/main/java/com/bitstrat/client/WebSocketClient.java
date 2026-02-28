package com.bitstrat.client;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.MessageType;
import com.bitstrat.domain.server.Message;
import com.bitstrat.handler.WebSocketClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.IdleStateEvent;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 15:47
 * @Content
 */

@Component
@Slf4j
@ChannelHandler.Sharable
public class WebSocketClient {
    @Value("${node.exchange-name}")
    private List<String> exchangeName;
    @Value("${node.auth}")
    private String auth;
    WebSocketClientHandler handler;

    private EventLoopGroup group;
    private URI uri = null;
    private Channel channel;

    public WebSocketClient(WebSocketClientHandler handler) {
        this.handler = handler;
    }

    public void init(String url) throws Exception {
        this.uri = new URI(url);
    }

    public void start() throws Exception {
        if(group == null) {
            group = new NioEventLoopGroup();
        }
        connect();
    }

    public void connect() throws Exception {


        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 80 : uri.getPort();

        HttpHeaders headers = new DefaultHttpHeaders();
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
            uri, WebSocketVersion.V13, null, true, headers);

        handler.setHandshaker(handshaker);

        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new HttpClientCodec());
                    p.addLast(new HttpObjectAggregator(8192));
                    p.addLast(new IdleStateHandler(0, 10, 0, TimeUnit.SECONDS)); // 20 秒发一次心跳
                    p.addLast(handler);
                }
            });

        ChannelFuture future = b.connect(host, port).sync();
        channel = future.channel();
        handler.handshakeFuture().sync(); // 等待握手完成

        log.info("✅ WebSocket连接成功: " + uri);

        // 发送AUTH
        for (String exchange : exchangeName) {
            Message message = new Message();
            message.setExchangeName(exchange);
            message.setAuth(auth);
            message.setType(MessageType.AUTH);
            message.setTimestamp(System.currentTimeMillis());
            String msg = JSONObject.toJSONString(message);
            sendText(msg);
        }


        // 等待关闭
        channel.closeFuture().sync();
        reconnect();
    }

    public void sendText(String text) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(new TextWebSocketFrame(text));
        }
    }

    public void reconnect() {
        log.info("bybit socket 尝试重连...");
        channel.close();

        // 使用 EventLoopGroup 延迟重连
        group.schedule(() -> {
            try {
                start(); // 重新连接
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, TimeUnit.SECONDS); // 5秒后重连
    }

    public Channel getChannel() {
        return channel;
    }

    @PreDestroy
    public void close() {
        log.info("正在关闭客户端");
        group.shutdownGracefully();
    }
}
