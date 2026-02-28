package com.bitstrat.handler;


import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.client.WebSocketClient;
import com.bitstrat.config.Monitor;
import com.bitstrat.constant.MessageType;
import com.bitstrat.domain.msg.ActiveLossPoint;
import com.bitstrat.domain.msg.PingData;
import com.bitstrat.domain.server.Message;
import com.bitstrat.service.MessageProcess;
import com.bitstrat.store.RoleCenter;
import com.bitstrat.store.RoleConfig;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 15:48
 * @Content
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    @Value("${node.exchange-name}")
    private List<String> exchangeName;
    @Value("${node.auth}")
    private String auth;

    @Value("${max-role-size}")
    private Long maxRoleSize;


    @Autowired
    RoleCenter roleCenter;


    @Autowired
    @Lazy
    private WebSocketClient webSocketClient;
    private  WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    @Autowired
        @Lazy
    MessageProcess messageProcess;


    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();

        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            log.info("WebSocket 握手成功");
            handshakeFuture.setSuccess();
            return;
        }

        if (msg instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) msg).text();
            log.info("📩 收到消息: {}",text );
            Message message = JSONObject.parseObject(text, Message.class);
            messageProcess.processMessage(message,ctx);

        } else if (msg instanceof PongWebSocketFrame) {
            log.info("💓 收到心跳响应（pong）");
        } else if (msg instanceof CloseWebSocketFrame) {
            log.info("🔌 服务端关闭连接");
            ch.close();
        }
    }

    public void triggerReportInfo() {
        Channel channel = webSocketClient.getChannel();
        for (String exchange : exchangeName) {
            Message message = new Message();
            message.setTimestamp(System.currentTimeMillis());
            message.setType(MessageType.PING);
            message.setAuth(auth);
            message.setExchangeName(exchange);
            ConcurrentHashMap<String, ActiveLossPoint> symbolLossPointMap = roleCenter.getSymbolLossPointMap();
            List<ActiveLossPoint> values = new ArrayList<>();
            for (ActiveLossPoint value : symbolLossPointMap.values()) {
                if (value.getExchangeName().equalsIgnoreCase(exchange)) {
                    values.add(value);
                }
            }

            PingData pingData = new PingData();
            pingData.setActiveLossPoints(values);
            pingData.setDelay(Monitor.delay.get());
            pingData.setMaxRoleSize(maxRoleSize);

            message.setData(pingData);
            String pingbody = JSONObject.toJSONString(message);
            channel.writeAndFlush(new TextWebSocketFrame(pingbody));
        }
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            log.info("💥 发送心跳 ping");
            ctx.channel().writeAndFlush(new PingWebSocketFrame());

            triggerReportInfo();

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        log.info("❌ 异常: {}", cause.getMessage());
        ctx.close();
//        webSocketClient.reconnect();
    }

    public void setHandshaker(WebSocketClientHandshaker handshakeFuture) {
        this.handshaker = handshakeFuture;
    }
}
