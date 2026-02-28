package com.bitstrat.holder;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.bitstrat.constant.SocketServerConstant.scheduledFutureAttributeKeyTest;

/**
 * WebSocketSession 用于保存当前所有在线的会话信息
 *
 * @author zendwang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebSocketSessionHolder {

    private static final Map<Long, ChannelGroup> USER_CHANNEL_GROUP_MAP = new ConcurrentHashMap<>();




    /**
     * 将WebSocket会话添加到用户会话Map中
     *
     * @param userId 会话键，用于检索会话
     * @param channel    要添加的WebSocket会话
     */
    public static void addSession(Long userId, Channel channel) {
        // 添加
        USER_CHANNEL_GROUP_MAP.computeIfAbsent(userId, k -> new DefaultChannelGroup(GlobalEventExecutor.INSTANCE))
            .add(channel);


    }

    /**
     * 从用户会话Map中移除指定会话键对应的WebSocket会话
     *
     * @param userId 要移除的会话键
     */
    public static void removeSession(Long userId) {
        if (userId == null) {
            return;
        }
        ChannelGroup remove = USER_CHANNEL_GROUP_MAP.remove(userId);
        if(remove != null) {
            try {
                remove.close();
            } catch (Exception ignored) {
            }
        }

    }/**
     * 从用户会话Map中移除指定会话键对应的WebSocket会话
     *
     * @param userId 要移除的会话键
     */
    public static void removeSession(Long userId,Channel channel) {
        if (userId == null) {
            return;
        }
        ChannelGroup channels = USER_CHANNEL_GROUP_MAP.getOrDefault(userId,null);

        if(channels != null) {
            channels.remove(channel);
        }

        ScheduledFuture<?> scheduledFuture = channel.attr(scheduledFutureAttributeKeyTest).get();
        if(scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    /**
     * 根据会话键从用户会话Map中获取WebSocket会话
     *
     * @param sessionKey 要获取的会话键
     * @return 与给定会话键对应的WebSocket会话，如果不存在则返回null
     */
    public static ChannelGroup getSessions(Long sessionKey) {
        return USER_CHANNEL_GROUP_MAP.getOrDefault(sessionKey,new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
    }

    /**
     * 获取存储在用户会话Map中所有WebSocket会话的会话键集合
     *
     * @return 所有WebSocket会话的会话键集合
     */
    public static Set<Long> getSessionsAll() {
        return USER_CHANNEL_GROUP_MAP.keySet();
    }

    /**
     * 检查给定的会话键是否存在于用户会话Map中
     *
     * @param sessionKey 要检查的会话键
     * @return 如果存在对应的会话键，则返回true；否则返回false
     */
    public static Boolean existSession(Long sessionKey) {
        return USER_CHANNEL_GROUP_MAP.containsKey(sessionKey);
    }
}
