package com.bitstrat.utils;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/21 18:59
 * @Content
 */

public class StringListUtil {
    public static boolean containsIgnoreCase(List<String> list, String target) {
        if (list == null || target == null) return false;
        return list.stream().anyMatch(s -> s.equalsIgnoreCase(target));
    }
}
