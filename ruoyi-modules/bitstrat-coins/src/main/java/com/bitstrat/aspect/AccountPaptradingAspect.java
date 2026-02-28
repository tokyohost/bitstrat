package com.bitstrat.aspect;

import com.bitstrat.annotation.AccountPaptrading;
import com.bitstrat.domain.Account;
import com.bitstrat.utils.APITypeHelper;
import com.bitstrat.utils.AccountUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
@Aspect
@Component
@Slf4j
public class AccountPaptradingAspect {

    /**
     * 匹配：
     * 1. 方法上有 @AccountPaptrading
     * 2. 类上有 @AccountPaptrading
     */
    @Pointcut("@annotation(com.bitstrat.annotation.AccountPaptrading) || @within(com.bitstrat.annotation.AccountPaptrading)")
    public void accountPapPointcut() {}


    @Around("accountPapPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        // 获取方法
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        // 先取方法上的注解
        AccountPaptrading ann = method.getAnnotation(AccountPaptrading.class);

        // 如果方法没有，则取类上的注解
        if (ann == null) {
            ann = joinPoint.getTarget().getClass().getAnnotation(AccountPaptrading.class);
        }

        // 如果类和方法都没有（极端情况），直接执行
        if (ann == null) {
            return joinPoint.proceed();
        }

        try {
            // 如果 ignore = true  直接跳过切面逻辑
            if (ann.ignore()) {
                return joinPoint.proceed();
            }

//            log.info("Before Account TypeSet call: {}", joinPoint.getSignature().toShortString());

            // 找入参中的 Account
            for (Object arg : joinPoint.getArgs()) {
                if (arg instanceof Account account) {

                    if (account != null && !StringUtils.isEmpty(account.getType())) {
                        APITypeHelper.set(account.getType());
                    } else {
                        log.error("Account type is empty: {} {}", account,joinPoint.getSignature().toShortString());
                    }
                }
            }

            Object result = joinPoint.proceed();

//            log.info("After Account TypeSet call: {}", joinPoint.getSignature().toShortString());

            return result;

        } finally {
            APITypeHelper.clear();
//            log.info("After Account TypeSet clear ok");
        }
    }
}

