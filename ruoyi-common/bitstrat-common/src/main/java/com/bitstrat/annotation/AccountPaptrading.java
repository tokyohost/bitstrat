package com.bitstrat.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 打上此注释后入参必须有 {@link com.bitstrat.domain.Account}
 * 在入参前进行模拟盘TYPE 配置
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AccountPaptrading {

    /**
     * 忽略此方法
     * @return
     */
    boolean ignore() default false;
}
