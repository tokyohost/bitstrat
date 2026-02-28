package com.bitstrat.store.listener;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 17:06
 * @Content
 */

@Component
public class SymbolWatch {
    ConcurrentHashMap<String, Long> symbolWatch = new ConcurrentHashMap<>();
    public SymbolWatch() {

    }

    public ConcurrentHashMap<String, Long> getSymbolWatch() {
        return symbolWatch;
    }
}
