package com.bitstrat.ai.utils;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/16 15:24
 * @Content
 */

public class DistuptorUtils {
    public static int nextPowerOfTwo(int value) {
        int highestOneBit = Integer.highestOneBit(value);
        return (value == highestOneBit) ? value : highestOneBit << 1;
    }
}
