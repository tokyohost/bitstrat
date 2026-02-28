package com.bitstrat.ai.server.handler;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/27 18:52
 * @Content
 */

import com.bitstrat.ai.config.ConnectionManager;
import com.bitstrat.ai.constant.SocketConstant;
import com.bitstrat.ai.domain.CompareContext;
import com.bitstrat.ai.domain.StartCompareContext;
import com.bitstrat.ai.utils.BusinessTypeProcess;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.bitstrat.ai.constant.SocketConstant.*;
import static com.bitstrat.ai.constant.SocketConstant.authFail;

@Component
@Slf4j
@ChannelHandler.Sharable
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Autowired
    private ConnectionManager connectionManager;
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
            CompareContext compareContext = BusinessTypeProcess.processBusinessType(ctx.channel().id() + "", paramMap);
            compareContext.setToken(paramMap.get("token"));
            LoginUser loginUser = LoginHelper.getLoginUser(compareContext.getToken());
            compareContext.setLoginUser(loginUser);
            StartCompareContext startCompareContext = BusinessTypeProcess.coverToStartContext(compareContext);
            startCompareContext.setChannel(channel);
            startCompareContext.setChannelHandlerContext(ctx);
            ctx.channel().attr(SocketConstant.COMPARE_CONTEXT).set(compareContext);
            SpringUtils.getApplicationContext().publishEvent(startCompareContext);
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
        boolean ok = connectionManager.authenticateAndRegister(ctx, clientId, token);
        if (ok) {
            log.info("客户端{} 鉴权通过", clientId);

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
}
