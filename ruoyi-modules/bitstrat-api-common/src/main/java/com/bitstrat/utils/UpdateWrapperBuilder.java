package com.bitstrat.utils;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/23 11:19
 * @Content
 */

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class UpdateWrapperBuilder {

    public static <T> LambdaUpdateWrapper<T> buildNonNullUpdateWrapper(T entity, SFunction<T, ?> idGetter) {
        LambdaUpdateWrapper<T> wrapper = new LambdaUpdateWrapper<>();

        try {
            Class<?> clazz = entity.getClass();
            Field[] fields = clazz.getDeclaredFields();

            Object idValue = idGetter.apply(entity);
            if (idValue == null) {
                throw new IllegalArgumentException("ID 不能为空");
            }

            wrapper.eq(idGetter, idValue);

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(entity);

                // 跳过 ID、自增、null、空字符串等
                if ("id".equals(field.getName()) || value == null) continue;
                if (value instanceof String && StringUtils.isBlank((String) value)) continue;

                String fieldName = field.getName();
                SFunction<T, ?> getter = getLambdaGetter(fieldName, clazz);
                if (getter != null) {
                    wrapper.set(getter, value);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("自动构建 updateWrapper 失败: " + e.getMessage(), e);
        }

        return wrapper;
    }

    private static final Map<String, SFunction<?, ?>> cache = new HashMap<>();

    @SuppressWarnings("unchecked")
    private static <T> SFunction<T, ?> getLambdaGetter(String fieldName, Class<?> clazz) {
        try {
            String key = clazz.getName() + "#" + fieldName;
            if (cache.containsKey(key)) {
                return (SFunction<T, ?>) cache.get(key);
            }

            // eg. getLongInFee → CoinsCrossExchangeArbitrageTask::getLongInFee
            String methodName = "get" + StringUtils.capitalize(fieldName);
            SFunction<T, ?> func = (SFunction<T, ?>) clazz.getMethod(methodName).invoke(null);
            cache.put(key, func);
            return func;
        } catch (Exception e) {
            // 无法通过反射获取 Lambda Getter，忽略
            return null;
        }
    }
}
