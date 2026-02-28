package com.bitstrat.wsClients.handler.okx;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.domain.Account;
import com.bitstrat.wsClients.constant.OKXApiConifg;
import com.bitstrat.wsClients.constant.SocketConstant;
import com.bitstrat.wsClients.domian.ConnectionConfig;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/27 11:21
 * @Content
 */
@ChannelHandler.Sharable
@Slf4j
public class OkxAuthHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    ConnectionConfig connectionConfig;

    public OkxAuthHandler(ConnectionConfig connectionConfig) {
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
                Map<String, Object> authMessage = createAuthMessage(account.getApiKey(), account.getApiSecret(),account.getPassphrase());
                if(authMessage == null) {
                    log.error("登录失败，请检查登录凭据 {} {}",connectionConfig.getUserId(),connectionConfig.getExchange());
                }
                String authBody = JSONObject.toJSONString(authMessage);
                ChannelFuture channelFuture = ctx.channel().writeAndFlush(new TextWebSocketFrame(authBody));
                channelFuture.addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("okx 鉴权已发送 {}",authBody);
                    }else{
                        log.error("okx 鉴权发送失败");
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

    private Map<String, Object> createAuthMessage(String apiKey, String apiSecret, String passphrase) {

        try {
            // 获取当前时间戳（秒）
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            // 构造待签名的字符串
            String prehash = timestamp + "GET" + "/users/self/verify";

            // 使用 HMAC SHA256 进行签名
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(prehash.getBytes(StandardCharsets.UTF_8));

            // 对签名结果进行 Base64 编码
            String sign = Base64.getEncoder().encodeToString(hash);

            // 构造认证消息
            Map<String, Object> args = new HashMap<>();
            args.put("apiKey", apiKey);
            args.put("passphrase", passphrase);
            args.put("timestamp", timestamp);
            args.put("sign", sign);

            Map<String, Object> authMessage = new HashMap<>();
            authMessage.put("op", "login");
            authMessage.put("args", Collections.singletonList(args));

            return authMessage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean requiresAuthentication(String path) {
        return OKXApiConifg.OKX_V5_WS_PRIVATE.equals(path);
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) {
//        String message = msg.text();
//        if (message.contains("login") && message.contains("success")) {
//            System.out.println("OKX authentication success");
//        } else {
//            System.out.println("OKX authentication failed");
//            ctx.close();
//        }
        String msg = textWebSocketFrame.text();
        Boolean b = ctx.channel().attr(SocketConstant.IS_AUTHENTICATED).get();
        if(b != null && b) {
            ctx.fireChannelRead(textWebSocketFrame.retain());
            return;
        }
        if(msg.contains("\"event\":\"error\"")){
            log.error("auth error message = {}",msg);
            //连接报错
            connectionConfig.getExchangeConnectionManager().closeConnection(connectionConfig.getUserId(),
                connectionConfig.getAccount().getId(),
                connectionConfig.getExchange()
            ,connectionConfig.getWebsocketType());
            connectionConfig.getAwaitConnectDone().countDown();
        }

        if (requiresAuthentication(connectionConfig.getUri().getPath()) && msg.contains("\"event\":\"login\"")) {
            log.info("auth message = {}",msg);
            // Check if authentication was successful
            boolean isAuthenticated = msg.contains("\"code\":\"0\"");
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

