package com.bitstrat.utils;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 15:23
 * @Content
 */

public class NettyUtils {
    public static String getClientIp(Channel channel) {
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        return socketAddress.getAddress().getHostAddress(); // 返回 IP 地址
    }
}
