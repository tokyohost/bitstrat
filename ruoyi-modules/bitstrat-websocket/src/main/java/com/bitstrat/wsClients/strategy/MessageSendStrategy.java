package com.bitstrat.wsClients.strategy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/27 16:58
 * @Content
 */
public interface MessageSendStrategy {
    public String getExchange();

    public void sendMessage(String userId, String exchange, String message, Channel channel);

    /**
     *
     * @param userId
     * @param exchange
     * @param message
     * @param channel
     * @param subscriptMsgType see{@link com.bitstrat.wsClients.constant.SubscriptMsgType}
     */
    public void sendSubscriptMessage(String userId, String exchange, String message, Channel channel,String subscriptMsgType);

}
