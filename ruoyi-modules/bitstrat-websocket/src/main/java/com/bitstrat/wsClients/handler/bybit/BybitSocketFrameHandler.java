package com.bitstrat.wsClients.handler.bybit;


import com.bitstrat.wsClients.domian.ConnectionConfig;
import com.bitstrat.wsClients.msg.receive.BybitReceiveMessage;
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
public class BybitSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private ConnectionConfig connectionConfig;

    public BybitSocketFrameHandler(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        // 处理WebSocket的消息
        String message = msg.text();
        log.info("[userid {} exchange {}] Received message: {}",connectionConfig.getUserId(),connectionConfig.getExchange(), message);
        BybitReceiveMessage bybitReceiveMessage = new BybitReceiveMessage();
        bybitReceiveMessage.setEx(connectionConfig.getExchange());
        bybitReceiveMessage.setMsg(message);
        bybitReceiveMessage.setUserId(connectionConfig.getUserId());
        bybitReceiveMessage.setConnectionConfig(connectionConfig);
        SpringUtils.getApplicationContext().publishEvent(bybitReceiveMessage);

        //处理完了就没必要往下触发了
//        ctx.fireChannelRead(msg.retain());
    }
}
