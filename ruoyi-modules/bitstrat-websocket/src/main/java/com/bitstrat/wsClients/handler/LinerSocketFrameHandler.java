package com.bitstrat.wsClients.handler;


import com.bitstrat.wsClients.constant.WebSocketType;
import com.bitstrat.wsClients.domian.ConnectionConfig;
import com.bitstrat.wsClients.msg.receive.LinerReceiveMsg;
import com.bitstrat.wsClients.msg.receive.OkxReceiveMessage;
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
public class LinerSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private ConnectionConfig connectionConfig;

    public LinerSocketFrameHandler(ConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        if (connectionConfig.getWebsocketType().equalsIgnoreCase(WebSocketType.LINER)) {
            // 处理Liner WebSocket的消息
            String message = msg.text();
//        log.info("[userid {} exchange {}] Received message: {}",connectionConfig.getUserId(),connectionConfig.getExchange(), message);
            LinerReceiveMsg linerReceiveMsg = new LinerReceiveMsg();
            linerReceiveMsg.setEx(connectionConfig.getExchange());
            linerReceiveMsg.setMsg(message);
            linerReceiveMsg.setUserId(connectionConfig.getUserId());
            linerReceiveMsg.setConnectionConfig(connectionConfig);
            SpringUtils.getApplicationContext().publishEvent(linerReceiveMsg);
        }else{
            ctx.fireChannelRead(msg.retain());
        }


    }
}
