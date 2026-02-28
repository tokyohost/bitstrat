package com.bitstrat.wsClients.handler.binance;


import com.bitstrat.wsClients.domian.ConnectionConfig;
import com.bitstrat.wsClients.msg.receive.FBinanceReceiveMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.SpringUtils;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/27 11:13
 * @Content
 */
@ChannelHandler.Sharable
@Slf4j
public class BinanceSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private ConnectionConfig connectionConfig;

    public BinanceSocketFrameHandler(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        // 处理WebSocket的消息
        String message = msg.text();
        log.info("[userid {} exchange {}] Received message: {}",connectionConfig.getUserId(),connectionConfig.getExchange(), message);
        FBinanceReceiveMessage binanceReceiveMessage = new FBinanceReceiveMessage();
        binanceReceiveMessage.setEx(connectionConfig.getExchange());
        binanceReceiveMessage.setMsg(message);
        binanceReceiveMessage.setUserId(connectionConfig.getUserId());
        binanceReceiveMessage.setConnectionConfig(connectionConfig);
        SpringUtils.getApplicationContext().publishEvent(binanceReceiveMessage);

        //处理完了就没必要往下触发了
//        ctx.fireChannelRead(msg.retain());
    }
}
