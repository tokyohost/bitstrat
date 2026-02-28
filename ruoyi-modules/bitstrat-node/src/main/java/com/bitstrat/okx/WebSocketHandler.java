package com.bitstrat.okx;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.cache.MarketPriceCache;
import com.bitstrat.okx.handle.OkxMessageHandler;
import com.bitstrat.okx.model.Constant.OkxChannelConstant;
import com.bitstrat.okx.model.Constant.SubscriptOp;
import com.bitstrat.okx.model.OkxSubscriptMsg;
import com.bitstrat.okx.model.SubscriptArg;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
@ChannelHandler.Sharable
public class WebSocketHandler extends SimpleChannelInboundHandler<Object> {

    private MarketPriceCache marketPriceCache;
    private OkxMessageHandler messageHandler;
    private final OkxWebSocketClient client;
    private WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketHandler(OkxWebSocketClient client,MarketPriceCache marketPriceCache
    ,OkxMessageHandler messageHandler) {
        this.marketPriceCache = marketPriceCache;
        this.client = client;
        this.messageHandler = messageHandler;
    }

    public void setHandshaker(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("okx 连接已建立");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.error("okx 连接断开，重连中...");
        client.reconnect();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();
            log.info("okx 握手成功");

//            // 订阅 BTC-USDT ticker
//            OkxSubscriptMsg okxSubscriptMsg = new OkxSubscriptMsg();
//            okxSubscriptMsg.setOp(SubscriptOp.SUBSCRIBE);
//            SubscriptArg subscriptArg = new SubscriptArg();
//            subscriptArg.setChannel(OkxChannelConstant.MARK_PRICE);
//            //币对
//            subscriptArg.setInstId("PROMPT-USDT-SWAP");
//            okxSubscriptMsg.setArgs(List.of(subscriptArg));
//            client.send(JSONObject.toJSONString(okxSubscriptMsg));

            return;
        }

        if (msg instanceof FullHttpResponse) {
            throw new IllegalStateException("Unexpected FullHttpResponse");
        }

        String payload = ((TextWebSocketFrame) msg).text();
        if (payload.contains("pong")) {
            log.info("okx 收到 pong");
        } else {
            log.info("okx 收到行情：" + payload);
            messageHandler.process(payload);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.WRITER_IDLE) {
                log.info("okx 发送 ping");
                ctx.channel().writeAndFlush(new TextWebSocketFrame("ping"));
            } else if (event.state() == IdleState.READER_IDLE) {
                log.error("okx 心跳超时");
                ctx.close(); // 触发断开重连
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
//        System.err.println("❗ 异常：" + cause.getMessage());
        ctx.close();
    }

    record SubscriptionMsg(String op, Arg args) {
        public SubscriptionMsg(String channel, String instId) {
            this("subscribe", new Arg(channel, instId));
        }
    }

    record Arg(String channel, String instId) {}
}
