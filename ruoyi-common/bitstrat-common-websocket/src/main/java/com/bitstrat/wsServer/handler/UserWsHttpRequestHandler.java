package com.bitstrat.wsServer.handler;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/27 18:52
 * @Content
 */

import com.bitstrat.holder.WebSocketSessionHolder;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.websocket.dto.AccountAutoSend;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.bitstrat.constant.SocketServerConstant.*;


@Component
@Slf4j
@ChannelHandler.Sharable
public class UserWsHttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        URI uri = new URI(request.uri());
        String query = uri.getQuery(); // 获取 ? 后的参数部分

        Map<String, String> paramMap = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    paramMap.put(kv[0], kv[1]);
                }
            }
        }

        // 保存参数到 Channel 上，后续 WebSocketHandler 可以用
        Channel channel = ctx.channel();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("token")) {
                channel.attr(TOKEN).set(entry.getValue());
            }else{
                channel.attr(AttributeKey.valueOf(entry.getKey())).set(entry.getValue());
            }
        }
        boolean b = channelAuth(ctx);
        if (b) {
            //处理业务
            LoginUser loginUser = LoginHelper.getLoginUser(paramMap.get("token"));

            // 放行请求给下一个 handler（比如 WebSocketServerProtocolHandler）
            ctx.fireChannelRead(request.retain());
        }else{
            return;
        }


    }
    public boolean channelAuth(ChannelHandlerContext ctx) {

        String clientId = ctx.channel().id() + "";
        String token = ctx.channel().attr(TOKEN).get();
        if(StringUtils.isEmpty(token)) {
            log.info("客户端{} 没有携带token", clientId);
            FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.UNAUTHORIZED,
                Unpooled.copiedBuffer("Unauthorized".getBytes())
            );
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
            response.headers().set("msg", authFailNoAuth);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return false;
        }
        boolean ok = authenticateAndRegister(ctx, clientId, token);
        if (ok) {
//            log.info("客户端{} 鉴权通过", clientId);

        } else {
            FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.UNAUTHORIZED,
                Unpooled.copiedBuffer("Unauthorized".getBytes())
            );
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
            response.headers().set("msg", authFail);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return false;

        }
        return true;
    }

    private boolean authenticateAndRegister(ChannelHandlerContext ctx, String clientId, String token) {
        LoginUser loginUser = LoginHelper.getLoginUser(token);
        if (Objects.isNull(loginUser)) {
            log.info("鉴权失败！");
            return false;
        }else{
            ctx.channel().attr(userId).set(loginUser.getUserId());
            WebSocketSessionHolder.addSession(loginUser.getUserId(), ctx.channel());
            //todo test
            ScheduledFuture<?> scheduledFuture = ctx.executor().scheduleWithFixedDelay(() -> {
                try{
                    AccountAutoSend accountAutoSend = new AccountAutoSend();
                    accountAutoSend.setUserId(loginUser.getUserId());
                    SpringUtils.getApplicationContext().publishEvent(accountAutoSend);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }, 5, 5, TimeUnit.SECONDS);
            ctx.channel().attr(scheduledFutureAttributeKeyTest).set(scheduledFuture);

            return true;
        }
    }
}
