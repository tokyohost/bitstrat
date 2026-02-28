package com.bitstrat.utils;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/21 15:03
 * @Content
 */


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ThreadLocalLogUtil {

    // 时间格式化器
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ThreadLocal 存储每个线程的日志集合
    private static final ThreadLocal<List<String>> LOG_HOLDER = ThreadLocal.withInitial(ArrayList::new);

    /**
     * 写入一条带时间戳的日志到当前线程的日志列表中
     * @param message 日志信息
     */
    public static void log(String message) {
        String timestampedMessage = String.format("%s - %s", now(), message);
        LOG_HOLDER.get().add(timestampedMessage);
    }

    /**
     * 获取当前线程的所有日志（返回副本）
     * @return 日志列表
     */
    public static List<String> getLogs() {
        return new ArrayList<>(LOG_HOLDER.get());
    }

    /**
     * 清空当前线程的日志
     */
    public static void clear() {
        LOG_HOLDER.remove();
    }

    /**
     * 获取当前时间字符串
     * @return 当前时间（yyyy-MM-dd HH:mm:ss 格式）
     */
    private static String now() {
        return LocalDateTime.now().format(FORMATTER);
    }
}
