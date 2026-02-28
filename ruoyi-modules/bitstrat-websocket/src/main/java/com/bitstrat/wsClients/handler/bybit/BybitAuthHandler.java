package com.bitstrat.wsClients.handler.bybit;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/27 11:21
 * @Content
 */
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.domain.Account;
import com.bitstrat.wsClients.constant.SocketConstant;
import com.bitstrat.wsClients.domian.ConnectionConfig;
import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.security.HmacSHA256Signer;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.bybit.api.client.constant.Helper.generateTransferID;

@ChannelHandler.Sharable
@Slf4j
public class BybitAuthHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private boolean authSent = false;
    ConnectionConfig connectionConfig;

    public BybitAuthHandler(ConnectionConfig connectionConfig) {
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
                Map<String, Object> authMessage = createAuthMessage(account.getApiSecurity(), account.getApiPwd());
                String authBody = JSONObject.toJSONString(authMessage);
                ChannelFuture channelFuture = ctx.channel().writeAndFlush(new TextWebSocketFrame(authBody));
                channelFuture.addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("bybit 鉴权已发送 {}",authBody);
                    }else{
                        log.error("bybit 鉴权发送失败");
                        future.cause().printStackTrace();
                        log.error(future.cause().getMessage());

                    }
                });
                authSent = true;
            }

        }else{
            //不需要鉴权
            connectionConfig.getAwaitConnectDone().countDown();
        }
        ctx.fireChannelActive();
    }
    private boolean requiresAuthentication(String path) {
        return BybitApiConfig.V5_TRADE.equals(path) ||
            BybitApiConfig.V5_PRIVATE.equals(path);
/*                BybitApiConfig.V3_CONTRACT_PRIVATE.equals(path) ||
                BybitApiConfig.V3_UNIFIED_PRIVATE.equals(path) ||
                BybitApiConfig.V3_SPOT_PRIVATE.equals(path);*/
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) {
        String msg = textWebSocketFrame.text();
        Boolean b = ctx.channel().attr(SocketConstant.IS_AUTHENTICATED).get();
        if(b != null && b) {
            ctx.fireChannelRead(textWebSocketFrame.retain());
            return;
        }

        if (requiresAuthentication(connectionConfig.getUri().getPath()) && msg.contains("\"op\":\"auth\"")) {
            log.info("auth message = {}",msg);
            // Check if authentication was successful
            boolean isAuthenticated = msg.contains("\"retCode\":0");
            boolean success = msg.contains("\"success\":true");
            if (isAuthenticated || success) {
                log.info("Authentication successful. userid {} exchange {}",connectionConfig.getUserId(),connectionConfig.getExchange());
                ctx.channel().attr(SocketConstant.IS_AUTHENTICATED).set(true);
                //发送需要鉴权的消息
                flushMessageQueue(ctx.channel());
            } else {
                log.error("Authentication failed. userid {} exchange {}",connectionConfig.getUserId(),connectionConfig.getExchange());
            }
            //通知已经建立连接了
            connectionConfig.getAwaitConnectDone().countDown();
        }else{
            ctx.fireChannelRead(textWebSocketFrame.retain());
        }

    }

    private String buildAuthMessage() {
        // 构造 Bybit 鉴权消息
        return "{\"op\": \"auth\", \"api_key\": \"your_api_key\", \"timestamp\": \"timestamp\", \"sign\": \"signature\"}";
    }
    private Map<String, Object> createAuthMessage(String apikey,String secret) {
        long expires = Instant.now().toEpochMilli() + 20000;
        String val = "GET/realtime" + expires;
        String signature = HmacSHA256Signer.getSignature(val, secret);

        var args = List.of(apikey, expires, signature);
        return Map.of("req_id", generateTransferID(), "op", "auth", "args", args);
    }
    private void flushMessageQueue(Channel channel) {
        List<String> messageQueue = channel.attr(SocketConstant.ByBitMsgQueue).get();
        if(Objects.isNull(messageQueue)) {
            return;
        }
        synchronized (messageQueue) {
            for (String msg : messageQueue) {
                channel.writeAndFlush(new TextWebSocketFrame(msg));
            }
            messageQueue.clear(); // Clear the queue after sending all messages
            channel.attr(SocketConstant.ByBitMsgQueue).setIfAbsent(messageQueue);
        }
    }
}

