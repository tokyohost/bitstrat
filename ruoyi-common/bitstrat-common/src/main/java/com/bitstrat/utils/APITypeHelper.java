package com.bitstrat.utils;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/11/19 10:51
 * @Content
 */
public class APITypeHelper {

    // 使用 TTL 替代 ThreadLocal
    private static final TransmittableThreadLocal<String> API_TYPE_HOLDER =
        new TransmittableThreadLocal<>();

    /**
     * 设置本次请求的 APIType
     */
    public static void set(String apiType) {
        API_TYPE_HOLDER.set(apiType);
    }

    /**
     * 获取并自动清除（避免污染后续请求）
     */
    public static String consume() {
        String value = API_TYPE_HOLDER.get();
        API_TYPE_HOLDER.remove();
        return value;
    }

    /**
     * 获取但不清除
     */
    public static String peek() {
        return API_TYPE_HOLDER.get();
    }

    /**
     * 手动清除
     */
    public static void clear() {
        API_TYPE_HOLDER.remove();
    }
}
