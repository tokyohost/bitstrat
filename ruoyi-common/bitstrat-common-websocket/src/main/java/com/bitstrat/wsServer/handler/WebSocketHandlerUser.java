package com.bitstrat.wsServer.handler;

import com.bitstrat.constant.SocketServerConstant;
import com.bitstrat.holder.WebSocketSessionHolder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bitstrat.constant.SocketServerConstant.userId;


/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 15:03
 * @Content
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class WebSocketHandlerUser extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        String receivedText = msg.text();
//        log.warn("回发客户端 {}",receivedText);
        // 回发客户端
        ctx.channel().writeAndFlush(msg.retain());
        ctx.fireChannelRead(msg.retain());
    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        log.info("客户端断开: " + ctx.channel().id());
        Long userId = ctx.channel().attr(SocketServerConstant.userId).get();
        WebSocketSessionHolder.removeSession(userId, ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Long userId = ctx.channel().attr(SocketServerConstant.userId).get();
        WebSocketSessionHolder.removeSession(userId, ctx.channel());
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
