package com.bitstrat.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/2/10 18:12
 * @Content
 */

public class UserWsServerConditional implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 根据某些条件判断是否匹配
        String env = context.getEnvironment().getProperty("bitstrat.user-websocket.enable");
        return "true".equals(env);
    }
}

