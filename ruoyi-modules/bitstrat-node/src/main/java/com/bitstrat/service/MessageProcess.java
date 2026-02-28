package com.bitstrat.service;

import com.bitstrat.domain.server.Message;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 18:15
 * @Content
 */


public interface MessageProcess {
    void processMessage(Message message, ChannelHandlerContext ctx);

    public void subscription(Message message);
}
