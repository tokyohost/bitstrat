package com.bitstrat.utils;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 20:45
 * @Content
 */

public class ProxyUtils {
    public static Object getRealObjectFromProxy(Object proxy) throws Exception {
        if (Proxy.isProxyClass(proxy.getClass())) {
            InvocationHandler invocationHandler = Proxy.getInvocationHandler(proxy);
            return invocationHandler;
        }
        return proxy;
    }
}
