package com.bitstrat.ai.constant;

import com.bitstrat.ai.domain.CompareContext;
import com.bitstrat.ai.domain.serverWatch.ServerWatchContext;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/27 19:04
 * @Content
 */

public class SocketConstant {

    public static  final  AttributeKey<String> TOKEN = AttributeKey.newInstance("token");
    public static  final  AttributeKey<CompareContext> COMPARE_CONTEXT = AttributeKey.newInstance("compareContext");
    public static  final  AttributeKey<ServerWatchContext> SERVER_WATCH_CONTEXT = AttributeKey.newInstance("ServerWatchContext");
    public static  final  AttributeKey<ScheduledFuture<?>> COMPARE_MARKET_PRICE_SCHEDULE = AttributeKey.newInstance("compareContextSchedule");
    public static  final  AttributeKey<ScheduledFuture<?>> COMPARE_SPREAD_RECORD_PRICE_SCHEDULE = AttributeKey.newInstance("compareSpreadRecordSchedule");
    public static final  ScheduledExecutorService newedScheduledThreadPool = Executors.newScheduledThreadPool(1);

    public static final String authSuccess = "{\"op\":\"auth\",\"status\":\"0\"}";
    public static final String authFail = "{\"op\":\"auth\",\"status\":\"1001\",\"msg\":\"鉴权失败\"}";
    public static final String authFailNoAuth = "{\"op\":\"auth\",\"status\":\"1002\",\"msg\":\"no auth\"}";
}
