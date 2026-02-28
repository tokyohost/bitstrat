package com.bitstrat.constant;

import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ScheduledFuture;

public class SocketServerConstant {

    public static  final AttributeKey<String> TOKEN = AttributeKey.newInstance("token");
    public static  final AttributeKey<Long> userId = AttributeKey.newInstance("userId");
    public static  final AttributeKey<ScheduledFuture<?>> scheduledFutureAttributeKeyTest = AttributeKey.newInstance("scheduledFuture");
    public static final String authSuccess = "{\"op\":\"auth\",\"status\":\"0\"}";
    public static final String authFail = "{\"op\":\"auth\",\"status\":\"1001\",\"msg\":\"鉴权失败\"}";
    public static final String authFailNoAuth = "{\"op\":\"auth\",\"status\":\"1002\",\"msg\":\"no auth\"}";

}
