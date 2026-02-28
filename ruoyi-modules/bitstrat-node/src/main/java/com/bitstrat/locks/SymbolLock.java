package com.bitstrat.locks;

import java.util.concurrent.ConcurrentHashMap;

public class SymbolLock {
    static final ConcurrentHashMap<String, Object> symbolLock = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, Object> getSymbolLock() {
        return symbolLock;
    }

    public static synchronized Object getlock(String symbol) {
        Object o = symbolLock.get(symbol);
        if(o == null){
            Object o1 = new Object();
            symbolLock.put(symbol,o1);
            return o1;
        }
        return o;
    }
}
