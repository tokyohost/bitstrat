package com.bitstrat.wsClients.handler.binance;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.wsClients.domian.ConnectionConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
@ChannelHandler.Sharable
public class BinanceDelyTestHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    ConnectionConfig connectionConfig;

    public BinanceDelyTestHandler(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    private String getPingData(String id){
        JSONObject pingData = new JSONObject();
        pingData.put("id", id);
        pingData.put("method", "LIST_SUBSCRIPTIONS");
        return pingData.toJSONString();
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
//        // 每 30 秒发送心跳
        ScheduledFuture<?> scheduledFuture = ctx.executor().scheduleAtFixedRate(() -> {
            String pingId = IdUtil.getSnowflake().nextIdStr();
            String pingData = getPingData(pingId);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(pingData));
            connectionConfig.setLastPingTimeStamp(System.currentTimeMillis());
            connectionConfig.setLastPingMsgId(pingId);
        }, 0, 30, TimeUnit.SECONDS);// 每 30 秒发送一次心跳
        connectionConfig.setDelyTestScheduledFuture(scheduledFuture);
        ctx.fireChannelActive();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) {
        String msg = textWebSocketFrame.text();
        if(msg.contains(connectionConfig.getLastPingMsgId())){
            log.info("receive msg websocket frame from 币安 userId :{} msg:{}", connectionConfig.getUserId(),msg);
            //什么都不用干，记录时间戳即可
            connectionConfig.setLastPongTimeStamp(System.currentTimeMillis());
            connectionConfig.setDely(connectionConfig.getLastPongTimeStamp() - connectionConfig.getLastPingTimeStamp());
            log.info("币安 dely time:{}",connectionConfig.getDely());
        }else{
            ctx.fireChannelRead(textWebSocketFrame.retain());
        }

    }
}
