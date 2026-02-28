package com.bitstrat.wsClients.constant;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.bitstrat.wsClients.domian.ConnectionConfig;
import com.bitstrat.wsClients.handler.WebSocketReconnectHandler;
import io.netty.util.AttributeKey;

import java.util.List;
import java.util.Set;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/27 14:57
 * @Content
 */

public class SocketConstant {
    public static  final  AttributeKey<Boolean> IS_AUTHENTICATED = AttributeKey.newInstance("isAuthenticated");
    public static  final  AttributeKey<Boolean> requiresAuthentication = AttributeKey.newInstance("requiresAuthentication");
    public static  final  AttributeKey<ConnectionConfig> connectionConfig = AttributeKey.newInstance("connectionConfig");
    public static  final  AttributeKey<WebSocketReconnectHandler> RECONNECT_HANDLER_ATTRIBUTE_KEY = AttributeKey.newInstance("reconnectHandlerAttributeKey");
    public static  final  AttributeKey<List<String>> ByBitMsgQueue = AttributeKey.newInstance("bybitMsgQueue");
    //订阅消息队列
    public static  final  AttributeKey<ConcurrentHashSet<String>> subscriptQueue = AttributeKey.newInstance("subscriptQueue");
}
