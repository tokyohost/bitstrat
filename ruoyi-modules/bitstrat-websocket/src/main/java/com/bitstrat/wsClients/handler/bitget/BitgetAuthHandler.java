package com.bitstrat.wsClients.handler.bitget;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.alibaba.fastjson2.JSONObject;
import com.bitget.openapi.common.enums.SignTypeEnum;
import com.bitget.openapi.common.utils.SignatureUtils;
import com.bitget.openapi.dto.request.ws.WsBaseReq;
import com.bitget.openapi.dto.request.ws.WsLoginReq;
import com.bitstrat.domain.Account;
import com.bitstrat.wsClients.constant.BitgetConstant;
import com.bitstrat.wsClients.constant.SocketConstant;
import com.bitstrat.wsClients.domian.ConnectionConfig;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/27 11:22
 * @Content
 */
@ChannelHandler.Sharable
@Slf4j
public class BitgetAuthHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    ConnectionConfig connectionConfig;

    public BitgetAuthHandler(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.channel().attr(SocketConstant.connectionConfig).setIfAbsent(connectionConfig);
        if (connectionConfig.isHandshakeComplete()) {
            connectionConfig.setHandshakeComplete(false);
        }else{
            log.error("握手没完成，忽略");
            ctx.fireChannelActive();
            return;
        }
        // 连接建立时发送鉴权消息
        if(requiresAuthentication(connectionConfig.getUri().getPath())) {
            ctx.channel().attr(SocketConstant.requiresAuthentication).set(true);
            ctx.channel().attr(SocketConstant.IS_AUTHENTICATED).set(false);
            Account account = connectionConfig.getAccount();
            if(account != null) {
                List<WsLoginReq> args = buildArgs(account);
                WsBaseReq<WsLoginReq> wsLoginReq = new WsBaseReq<>(BitgetConstant.WS_OP_LOGIN, args);
                String authBody = JSONObject.toJSONString(wsLoginReq);
                log.info("bitget 鉴权消息： {}",authBody);
                ChannelFuture channelFuture = ctx.channel().writeAndFlush(new TextWebSocketFrame(authBody));
                channelFuture.addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("bitget 鉴权已发送 {}",authBody);
                    }else{
                        log.error("bitget 鉴权发送失败");
                        future.cause().printStackTrace();
                        log.error(future.cause().getMessage());
                    }
                });
            }

        }else{
            //不需要鉴权
            connectionConfig.getAwaitConnectDone().countDown();
        }
        ctx.fireChannelActive();
    }
    private List<WsLoginReq> buildArgs(Account account) {
        String timestamp = Long.valueOf(Instant.now().getEpochSecond()).toString();
        String sign = sha256_HMAC(timestamp, account.getApiSecret());
        if (SignTypeEnum.RSA.getName() == account.getSignType()) {
            sign = ws_rsa(timestamp, account.getApiSecret());
        }

        WsLoginReq loginReq = WsLoginReq.builder().apiKey(account.getApiKey()).passphrase(account.getPassphrase()).timestamp(timestamp).sign(sign).build();

        List<WsLoginReq> args = new ArrayList<WsLoginReq>() {{
            add(loginReq);
        }};
        return args;
    }
    private String sha256_HMAC(String timeStamp, String secret) {
        String hash = "";
        try {
            hash = SignatureUtils.wsGenerateSign(timeStamp, secret);
        } catch (Exception e) {
            throw new RuntimeException("sha256_HMAC error", e);
        }
        return hash;
    }

    private String ws_rsa(String timeStamp, String secret) {
        String hash = "";
        try {
            hash = SignatureUtils.wsGenerateRsaSignature(timeStamp, secret);
        } catch (Exception e) {
            throw new RuntimeException("sha256_HMAC error", e);
        }
        return hash;
    }

    private boolean requiresAuthentication(String path) {
        if (path.equalsIgnoreCase("/v2/ws/public")) {
            // 公共不需要鉴权
            return false;
        }
        return true;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) {
        String msg = textWebSocketFrame.text();
        Boolean b = ctx.channel().attr(SocketConstant.IS_AUTHENTICATED).get();
        if(b != null && b) {
            ctx.fireChannelRead(textWebSocketFrame.retain());
            return;
        }
        if(msg.contains("\"event\":\"error\"")){
            //连接报错
            log.error("auth error message = {}",msg);
            connectionConfig.getExchangeConnectionManager().closeConnection(connectionConfig.getUserId(),
                connectionConfig.getAccount().getId(),
                connectionConfig.getExchange()
                ,connectionConfig.getWebsocketType());
            connectionConfig.getAwaitConnectDone().countDown();
        }

        if (requiresAuthentication(connectionConfig.getUri().getPath()) && msg.contains("\"event\":\"login\"")) {
            log.info("auth message = {}",msg);
            // Check if authentication was successful
            boolean isAuthenticated = msg.contains("\"code\":0");
            if (isAuthenticated) {
                log.info("[{} {}]Authentication successful. ",connectionConfig.getUserId(),connectionConfig.getExchange());
                ctx.channel().attr(SocketConstant.IS_AUTHENTICATED).set(true);
                //发送需要鉴权的消息
                flushMessageQueue(ctx.channel());
            } else {
                log.error("[{} {}]Authentication failed. ",connectionConfig.getUserId(),connectionConfig.getExchange());
                connectionConfig.getAwaitConnectDone().countDown();
                //鉴权失败，重连
                ctx.channel().close();
            }
            //通知已经建立连接了
            connectionConfig.getAwaitConnectDone().countDown();
        }else{

            ctx.fireChannelRead(textWebSocketFrame.retain());
        }

    }

    private void flushMessageQueue(Channel channel) {

        ConcurrentHashSet<String> messageQueue = channel.attr(SocketConstant.subscriptQueue).get();
        if(Objects.isNull(messageQueue)) {
            return;
        }
        synchronized (messageQueue) {
            for (String msg : messageQueue) {
                log.info("[{} {} msg:{}] ",connectionConfig.getUserId(),connectionConfig.getExchange(),msg);
                channel.writeAndFlush(new TextWebSocketFrame(msg));
            }
            messageQueue.clear(); // Clear the queue after sending all messages
            channel.attr(SocketConstant.subscriptQueue).setIfAbsent(messageQueue);
        }
    }

}

