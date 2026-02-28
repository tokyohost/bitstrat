package com.bitstrat.event;

import io.netty.channel.Channel;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/12 10:38
 * @Content
 */
@FunctionalInterface
public interface  ChannelSend {
    void handle(Channel channel);
}
