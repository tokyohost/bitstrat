package org.dromara.common.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/16 15:48
 * @Content
 */
//@Configuration
//@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // 配置 Caffeine 缓存
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(20, TimeUnit.SECONDS) // 设置缓存过期时间
            .maximumSize(1000) // 最大缓存数量
            .recordStats()); // 记录统计信息（例如缓存命中、缓存未命中等）

        return cacheManager;
    }
}
