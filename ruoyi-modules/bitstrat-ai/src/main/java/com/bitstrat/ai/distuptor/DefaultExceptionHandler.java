package com.bitstrat.ai.distuptor;

import com.lmax.disruptor.ExceptionHandler;

public class DefaultExceptionHandler<T> implements ExceptionHandler<T> {

    // 处理事件时的异常（onEvent 方法中抛出的异常）
    @Override
    public void handleEventException(Throwable ex, long sequence, T event) {
        System.err.println("【事件处理异常】Sequence: " + sequence + ", Event: " + event);
        ex.printStackTrace();
        // 可添加日志、报警、事件补偿等逻辑
    }

    // 启动 disruptor 时出现的异常
    @Override
    public void handleOnStartException(Throwable ex) {
        System.err.println("【启动异常】Disruptor start failed");
        ex.printStackTrace();
    }

    // 关闭 disruptor 时出现的异常
    @Override
    public void handleOnShutdownException(Throwable ex) {
        System.err.println("【关闭异常】Disruptor shutdown failed");
        ex.printStackTrace();
    }
}
