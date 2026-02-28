package org.dromara.test;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/27 11:12
 * @Content
 */

@Slf4j
public class CaffeineCacheExample {
    public static void main(String[] args) throws InterruptedException {
        Cache<String, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)  // 指定写入后 5 分钟过期
            .removalListener((String key, String value, RemovalCause cause) -> {
                if (cause == RemovalCause.EXPIRED) {
                    System.out.println("过期通知 -> key: " + key + ", value: " + value);
                }
            })
            .build();

        // 写入缓存
        cache.put("user1", "Alice");

        // 访问缓存
        System.out.println("Initial value: " + cache.getIfPresent("user1"));
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(()->{
            System.out.println("定时清除");
            cache.cleanUp();
        },1,2,TimeUnit.SECONDS);


        // 等待超时（6秒 > 5秒过 期时间）
        Thread.sleep(12000);

        // 再次访问触发过期检查
//        System.out.println("Access after expiry: " + cache.getIfPresent("user1"));
    }
}
