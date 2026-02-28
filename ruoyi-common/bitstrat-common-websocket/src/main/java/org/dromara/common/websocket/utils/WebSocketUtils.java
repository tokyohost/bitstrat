package org.dromara.common.websocket.utils;

import cn.hutool.core.util.IdUtil;
import com.bitstrat.domain.WebSocketMessageCover;
import com.bitstrat.domain.msg.PongMessage;
import com.bitstrat.holder.WebSocketSessionHolder;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.ServerInstanceUtil;
import org.dromara.common.redis.utils.RedisUtils;
import org.dromara.common.websocket.dto.WebSocketMessageDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.bitstrat.constant.WebSocketConstants.WEB_SOCKET_TOPIC;


/**
 * 工具类
 *
 * @author zendwang
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebSocketUtils {

    /**
     * 向指定的WebSocket会话发送消息
     *
     * @param sessionKey 要发送消息的用户id
     * @param message    要发送的消息内容
     */
    public static void sendMessage(Long sessionKey, String message) {
//        ChannelGroup sessions = WebSocketSessionHolder.getSessions(sessionKey);

//        sendMessage(sessions, message);
        //广播
        WebSocketMessageDto webSocketMessageDto = new WebSocketMessageDto();
        webSocketMessageDto.setMessage(message);
        webSocketMessageDto.setIgnoreInstanceId(ServerInstanceUtil.SERVER_INSTANCE_ID);
        webSocketMessageDto.setMsgId(IdUtil.getSnowflakeNextIdStr());
        webSocketMessageDto.setSessionKeys(List.of(sessionKey));
        publishMessage(webSocketMessageDto);

    }
    /**
     * 向本机的WebSocket会话发送消息
     *
     * @param sessionKey 要发送消息的用户id
     * @param message    要发送的消息内容
     */
    public static void sendLocalMessage(Long sessionKey, String message) {
        ChannelGroup sessions = WebSocketSessionHolder.getSessions(sessionKey);

        sendMessage(sessions, message);
    }
    /**
     * 向指定的WebSocket会话发送消息
     *
     * @param sessionKey 要发送消息的用户id
     * @param message    要发送的消息内容
     */
    public static void sendMessageBySubscribe(Long sessionKey, String message) {
        ChannelGroup sessions = WebSocketSessionHolder.getSessions(sessionKey);
        if (Objects.isNull(sessions) || sessions.isEmpty()) {
            log.debug("集群ws 消息 sessionId {} 不在当前节点存在连接，已丢弃",sessionKey);
            return;
        }
        sendMessage(sessions, message);

    }

    /**
     * 订阅WebSocket消息主题，并提供一个消费者函数来处理接收到的消息
     *
     * @param consumer 处理WebSocket消息的消费者函数
     * @deprecated  会出现断连情况
     */
    @Deprecated
    public static void subscribeMessage(Consumer<WebSocketMessageDto> consumer) {
        log.warn("集群消息订阅已完成...");
        RedisUtils.subscribe(WEB_SOCKET_TOPIC, WebSocketMessageDto.class, consumer);
    }

    /**
     * 发布WebSocket订阅消息
     *
     * @param webSocketMessage 要发布的WebSocket消息对象
     */
    public static void publishMessage(WebSocketMessageDto webSocketMessage) {
        List<Long> unsentSessionKeys = new ArrayList<>();
        // 当前服务内session,直接发送消息
        for (Long sessionKey : webSocketMessage.getSessionKeys()) {
            if (WebSocketSessionHolder.existSession(sessionKey)) {
                WebSocketUtils.sendLocalMessage(sessionKey, webSocketMessage.getMessage());
                continue;
            }
            unsentSessionKeys.add(sessionKey);
        }
        // 无论什么情况下都要发送订阅消息，用户可能多个连接连到了不同的节点
//        if (CollUtil.isNotEmpty(unsentSessionKeys)) {
        webSocketMessage.setMessage(webSocketMessage.getMessage());
        webSocketMessage.setIgnoreInstanceId(ServerInstanceUtil.SERVER_INSTANCE_ID);
            RedisUtils.publish(WEB_SOCKET_TOPIC, webSocketMessage, consumer -> {
                log.info(" WebSocket发送主题订阅消息topic:{} session keys:{} message:{}",
                    WEB_SOCKET_TOPIC, webSocketMessage.getSessionKeys(), webSocketMessage.getMessage());
            });
//        }
    }

    /**
     * 向所有的WebSocket会话发布订阅的消息(群发)
     *
     * @param message 要发布的消息内容
     */
    @Deprecated
    public static void publishAll(String message) {
        WebSocketMessageDto broadcastMessage = new WebSocketMessageDto();
        broadcastMessage.setMessage(message);
        RedisUtils.publish(WEB_SOCKET_TOPIC, broadcastMessage, consumer -> {
            log.info("WebSocket发送主题订阅消息topic:{} message:{}", WEB_SOCKET_TOPIC, message);
        });
    }

    /**
     * 向指定的WebSocket会话发送Pong消息
     *
     * @param session 要发送Pong消息的WebSocket会话
     */
    public static void sendPongMessage(ChannelGroup session) {
        if (session == null || session.isEmpty()) {
            log.warn("[send] session会话已经关闭");
        } else {
            session.writeAndFlush(new TextWebSocketFrame(new PongMessage().toJSONString()));
        }
    }

    /**
     * 向指定的WebSocket会话发送文本消息
     *
     * @param session WebSocket会话
     * @param message 要发送的文本消息内容
     */
    public static void sendMessage(ChannelGroup session, WebSocketMessageCover<?> message) {

        sendMessage(session, message.toJSONString());
    }

    /**
     * 向指定的WebSocket会话发送WebSocket消息对象
     *
     * @param session WebSocket会话
     * @param message 要发送的WebSocket消息对象
     */
    private synchronized static void sendMessage(ChannelGroup session, String message) {
        if (session == null || session.isEmpty()) {
            log.warn("[send] session会话已经关闭");
        } else {
            session.writeAndFlush(new TextWebSocketFrame(message));
        }
    }
}
