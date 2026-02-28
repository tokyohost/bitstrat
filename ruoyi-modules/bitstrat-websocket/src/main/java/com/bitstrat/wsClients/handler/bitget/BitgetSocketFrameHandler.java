package com.bitstrat.wsClients.handler.bitget;


import com.bitstrat.wsClients.domian.ConnectionConfig;
import com.bitstrat.wsClients.msg.receive.BitgetReceiveMessage;
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
public class BitgetSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private ConnectionConfig connectionConfig;

    public BitgetSocketFrameHandler(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        // 处理WebSocket的消息
        String message = msg.text();
//        log.info("[userid {} exchange {}] Received message: {}",connectionConfig.getUserId(),connectionConfig.getExchange(), message);
        BitgetReceiveMessage bitgetReceiveMessage = new BitgetReceiveMessage();
        bitgetReceiveMessage.setEx(connectionConfig.getExchange());
        bitgetReceiveMessage.setMsg(message);
        bitgetReceiveMessage.setUserId(connectionConfig.getUserId());
        bitgetReceiveMessage.setConnectionConfig(connectionConfig);
        SpringUtils.getApplicationContext().publishEvent(bitgetReceiveMessage);

        //处理完了就没必要往下触发了
//        ctx.fireChannelRead(msg.retain());
    }
}
