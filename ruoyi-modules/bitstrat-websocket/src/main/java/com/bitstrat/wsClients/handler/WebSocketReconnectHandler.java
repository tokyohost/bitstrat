package com.bitstrat.wsClients.handler;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/27 11:25
 * @Content
 */

import com.bitstrat.event.ChannelSend;
import com.bitstrat.wsClients.WebSocketChannelInitializer;
import com.bitstrat.wsClients.constant.SocketConstant;
import com.bitstrat.wsClients.domian.ConnectionConfig;
import com.bitstrat.wsClients.utils.AckPositionUtils;
import com.bitstrat.wsClients.utils.WsCheck;
import io.github.linpeilie.utils.CollectionUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ChannelHandler.Sharable
@Slf4j
public class WebSocketReconnectHandler extends ChannelInboundHandlerAdapter {
    private boolean handshakeComplete = false;
    private final ConnectionConfig connectionConfig;
    private volatile boolean isManualClose = false;

    public WebSocketReconnectHandler(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        connectionConfig.setChannel(ctx.channel());
        // 检查握手状态
        if (handshakeComplete) {
            ctx.fireChannelActive();
        } else {
            log.info("handshake not complete");
        }


    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("连接断开, userId=" + connectionConfig.getUserId() + ", exchange=" + connectionConfig.getExchange());
        ScheduledFuture<?> heatebeatScheduledFuture = connectionConfig.getHeatebeatScheduledFuture();
        if (Objects.nonNull(heatebeatScheduledFuture)) {
            heatebeatScheduledFuture.cancel(true);
        }

        ScheduledFuture<?> delyTestScheduledFuture = connectionConfig.getDelyTestScheduledFuture();
        if (Objects.nonNull(delyTestScheduledFuture)) {
            delyTestScheduledFuture.cancel(true);
        }

        ScheduledFuture<?> positionScheduledFuture = connectionConfig.getPositionSyncScheduledFuture();
        if (Objects.nonNull(positionScheduledFuture)) {
            positionScheduledFuture.cancel(true);
        }

        if (!isManualClose) {
            reconnect(ctx);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        log.error("连接异常: " + cause.getMessage());
        ctx.close();
    }

    public void close(Channel channel) {
        ScheduledFuture<?> heatebeatScheduledFuture = connectionConfig.getHeatebeatScheduledFuture();
        if (Objects.nonNull(heatebeatScheduledFuture)) {
            heatebeatScheduledFuture.cancel(true);
        }
        ScheduledFuture<?> delyTestScheduledFuture = connectionConfig.getDelyTestScheduledFuture();
        if (Objects.nonNull(delyTestScheduledFuture)) {
            delyTestScheduledFuture.cancel(true);
        }
        isManualClose = true;
        channel.close();
    }

    private void reconnect(ChannelHandlerContext ctx) {
        connectionConfig.setSubscriptQueue(ctx.channel().attr(SocketConstant.subscriptQueue).get());
        ctx.channel().eventLoop().schedule(() -> {
            if (connectionConfig.getCurrentReconnectTimes().get() > connectionConfig.getMaxReconnectTimes()) {
                //重连次数太多次了停止
                log.info("重连次数超过最大限制，停止重连 userId=" + connectionConfig.getUserId() + ", exchange=" + connectionConfig.getExchange());
                connectionConfig.offLineUser();
                close(ctx.channel());
                return;
            }

            connectionConfig.getCurrentReconnectTimes().incrementAndGet();
            log.info("尝试重连 userId=" + connectionConfig.getUserId() + ", exchange=" + connectionConfig.getExchange()+" reconnectTimes ={}",connectionConfig.getCurrentReconnectTimes().get());
            URI uri = connectionConfig.getSourceUri();
            ScheduledFuture<?> heatebeatScheduledFuture = connectionConfig.getHeatebeatScheduledFuture();
            if (Objects.nonNull(heatebeatScheduledFuture)) {
                heatebeatScheduledFuture.cancel(true);
            }
            CountDownLatch awaitConnectDone = new CountDownLatch(1);
            connectionConfig.setAwaitConnectDone(awaitConnectDone);
            Bootstrap bootstrap = new Bootstrap().group(connectionConfig.getLoopGroup())
                .channel(NioSocketChannel.class).handler(new WebSocketChannelInitializer(connectionConfig));
            uri = WsCheck.preAuthWebsocket(connectionConfig.getAccount(), connectionConfig.getExchange(), connectionConfig.getWebsocketType(), uri);
            int port = uri.getPort();
            if (uri.getPort() < 0) {
                if (uri.getScheme().equals("ws")) {
                    port = 80;
                } else if (uri.getScheme().equals("wss")) {
                    port = 443;
                } else {
                    throw new RuntimeException("Invalid URI port");
                }
            }

            ChannelFuture channelFuture = bootstrap.connect(uri.getHost(), port).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
//                    log.info("重连成功！");

                } else {
                    future.cause().printStackTrace();
                    log.info("重连失败，继续重试...");
                    awaitConnectDone.countDown();
                    reconnect(ctx);
                }
            });
            Channel channel = channelFuture.channel();
            channel.attr(SocketConstant.subscriptQueue).set(connectionConfig.getSubscriptQueue());
            channel.attr(SocketConstant.ByBitMsgQueue).set(ctx.channel().attr(SocketConstant.ByBitMsgQueue).get());
            try {
                boolean await = awaitConnectDone.await(10, TimeUnit.SECONDS);
                if (await == false) {
                    log.info("重连等待超时。。。 userId={} apiId={}",connectionConfig.getUserId(),connectionConfig.getAccount().getId());
                    channel.close();
//                    reconnect(ctx);
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.info("重连超时。。。 userId={} apiId={}",connectionConfig.getUserId(),connectionConfig.getAccount().getId());
                channel.close();
//                reconnect(ctx);
                return;
            }
            connectionConfig.setChannel(channel);
            connectionConfig.getExchangeConnectionManager().addChannelToManager(
                connectionConfig.getUserId(),
                connectionConfig.getAccount().getId(),
                connectionConfig.getExchange(),
                connectionConfig.getWebsocketType(),
                channel
            );
            //检查订阅消息
//            Set<String> subs = channel.attr(SocketConstant.subscriptQueue).get();
//            if (CollectionUtils.isNotEmpty(subs)) {
//                log.info("重连成功，重发订阅");
//                for (String sub : subs) {
//                    channel.writeAndFlush(new TextWebSocketFrame(sub));
//                }
//            }


            ChannelSend subscriptCallback = connectionConfig.getSubscriptCallback();
            log.info("重连成功，检查是否存在订阅回调");
            if(Objects.nonNull(subscriptCallback)){
                log.info("有订阅回调  userId={} exchange{}",connectionConfig.getUserId(),connectionConfig.getExchange());
                subscriptCallback.handle(channel);
            }else{
                log.info("无订阅回调  userId={} exchange{}",connectionConfig.getUserId(),connectionConfig.getExchange());
            }
            //检查是否存在主动同步仓位的任务
            ScheduledFuture<?> positionSyncScheduledFuture = connectionConfig.getPositionSyncScheduledFuture();
            if(Objects.nonNull(positionSyncScheduledFuture)){
                NioEventLoopGroup loopGroup = connectionConfig.getLoopGroup();
                ScheduledFuture<?> scheduledFuture = AckPositionUtils.startAckPosition(loopGroup, connectionConfig, 5L, 30L);
                positionSyncScheduledFuture.cancel(true);
                connectionConfig.setPositionSyncScheduledFuture(scheduledFuture);
            }
            if(channel.isActive()){
                connectionConfig.setCurrentReconnectTimes(new AtomicInteger(0));
            }else{
                reconnect(ctx);
            }

        }, 10, TimeUnit.SECONDS);
    }

    private int getPort(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        return "wss".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketClientProtocolHandler.ClientHandshakeStateEvent) {
            WebSocketClientProtocolHandler.ClientHandshakeStateEvent handshakeStateEvent = (WebSocketClientProtocolHandler.ClientHandshakeStateEvent) evt;
            if (handshakeStateEvent == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
                // 握手完成
                handshakeComplete = true;
                connectionConfig.setHandshakeComplete(true);
                log.info("handshake complete");
                ctx.fireChannelActive();
            }

        }


        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.READER_IDLE) {
                log.info("检测到读空闲，主动关闭并重连，userId=" + connectionConfig.getUserId() + ", exchange=" + connectionConfig.getExchange());
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}


