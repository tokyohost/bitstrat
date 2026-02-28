package com.bitstrat.server.handler;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.config.DeviceConnectionManager;
import com.bitstrat.domain.DeviceInfo;
import com.bitstrat.domain.msg.ActiveLossPoint;
import com.bitstrat.domain.msg.PingData;
import com.bitstrat.domain.server.Message;
import com.bitstrat.domain.server.MessageData;
import com.bitstrat.store.RoleCenter;
import com.bitstrat.utils.NettyUtils;
import com.bitstrat.utils.ProxyUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static com.bitstrat.constant.MessageType.*;
import static com.bitstrat.constant.ServiceConstant.CLIENT_ID_ATTR;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 15:03
 * @Content
 */
@Slf4j
@Component
@ChannelHandler.Sharable
@Deprecated
public class WebSocketHandlerBitstrat extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Autowired
    private DeviceConnectionManager connectionManager;

    @Autowired
    RoleCenter roleCenter;
    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        String receivedText = msg.text();
        log.info("收到消息: " + receivedText);
        Message message = JSONObject.parseObject(receivedText, Message.class);
        switch (message.getType()) {
            case AUTH:
                //处理认证
                handleConnect(ctx, message);
                break;
            case PING:
                //处理PING
                handlePingReq(ctx,message);
                break;
            case DISCONNECT:
                //处理离线
                handleDisconnect(ctx);
                break;
        }

        // 回发客户端
//        ctx.channel().writeAndFlush(new TextWebSocketFrame(receivedText));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        log.info("客户端连接: " + ctx.channel().id());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        connectionManager.handleDeviceDisconnection(ctx.channel().attr(CLIENT_ID_ATTR).get(),ctx.channel());
        log.info("客户端断开: " + ctx.channel().id());
    }

    private void handleConnect(ChannelHandlerContext ctx, Message msg) {
        String clientId = msg.getAuth();
        log.info("Device attempting to connect with auth: {}", clientId);

        if (isValidClient(clientId, msg)) {

            connectionManager.handleDeviceConnection(clientId,msg, ctx.channel());
            log.info("Device connected successfully : {} IP: {}", clientId, NettyUtils.getClientIp(ctx.channel()));
        } else {
            log.warn("Device connection rejected: {}", clientId);
        }

    }

    private void handlePublish(ChannelHandlerContext ctx, Message msg) {
        String clientId = ctx.channel().attr(CLIENT_ID_ATTR).get();

        log.info("Received publish message - ClientID: {}", clientId);

        try {
            // 读取消息内容
            MessageData content = msg.getData();

            log.info("Message content: {}", content);

        } catch (Exception e) {
            log.error("Error processing publish message from device {}", clientId, e);
            e.printStackTrace();
        }
    }

    private void handleDisconnect(ChannelHandlerContext ctx) {
        String clientId = ctx.channel().attr(CLIENT_ID_ATTR).get();
        log.info("Device disconnecting: {}", clientId);

        connectionManager.handleDeviceDisconnection(clientId,ctx.channel());
        ctx.close();

        log.info("Device disconnected: {}", clientId);
    }

    @SneakyThrows
    private void handlePingReq(ChannelHandlerContext ctx, Message message) {
        String clientId = ctx.channel().attr(CLIENT_ID_ATTR).get();
        connectionManager.refreshDevice(ctx.channel());
        log.debug("Responded to ping from device: {}", clientId);
        long timestamp = Instant.now().toEpochMilli();
        Long timestampDevice = message.getTimestamp();
        Object realObjectFromProxy = ProxyUtils.getRealObjectFromProxy(message.getData());
        JSONObject from = JSONObject.from(realObjectFromProxy);
        PingData data = from.to(PingData.class);
        List<ActiveLossPoint> activeLossPoints = data.getActiveLossPoints();
        if (activeLossPoints.isEmpty()) {
            roleCenter.getSymbolLossPointMap().clear();
        }
        for (ActiveLossPoint activeLossPoint : activeLossPoints) {
            roleCenter.put(message.getExchangeName(),activeLossPoint.getSymbol(),activeLossPoint.getId(),clientId,activeLossPoint);
        }
        roleCenter.getNodeLossPointMap().put(message.getAuth(), activeLossPoints);

        String exchangeName = message.getExchangeName();


        long sub = timestamp - timestampDevice;
        DeviceInfo deviceInfo = connectionManager.getDeviceInfo(clientId);
        deviceInfo.setNodeToServerDelay(sub);
        deviceInfo.setDelay(data.getDelay());
        deviceInfo.setMaxRoleSize(data.getMaxRoleSize());



    }

    private boolean isValidClient(String clientId, Message msg) {
        // TODO: 实现实际的设备认证逻辑
        return true;
    }
}
