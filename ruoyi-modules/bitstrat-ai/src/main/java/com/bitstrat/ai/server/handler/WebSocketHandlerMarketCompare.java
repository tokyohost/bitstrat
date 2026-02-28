package com.bitstrat.ai.server.handler;

import com.bitstrat.ai.config.ConnectionManager;
import com.bitstrat.ai.constant.SocketConstant;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.bitstrat.ai.constant.SocketConstant.*;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 15:03
 * @Content
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class WebSocketHandlerMarketCompare extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Autowired
    private ConnectionManager connectionManager;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        String receivedText = msg.text();
        log.warn("回发客户端 {}",receivedText);
        // 回发客户端
        ctx.channel().writeAndFlush(msg.retain());
        ctx.fireChannelRead(msg.retain());
    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        log.info("客户端断开: " + ctx.channel().id());
        connectionManager.remove(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connectionManager.remove(ctx.channel());
        super.channelInactive(ctx);
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;

            if (event.state() == IdleState.READER_IDLE) {
                // 说明 60 秒内没有收到客户端任何数据，主动关闭连接
                log.warn("60s 内无客户端消息，关闭连接：{} id:{}", ctx.channel().remoteAddress(),ctx.channel().id());
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
