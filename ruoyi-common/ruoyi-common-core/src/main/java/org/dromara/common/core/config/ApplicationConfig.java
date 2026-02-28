package org.dromara.common.core.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 程序注解配置
 *
 * @author Lion Li
 */
@AutoConfiguration
@EnableAspectJAutoProxy
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@ComponentScans({
    @ComponentScan("com.dromara.**"),
    @ComponentScan("com.bitstrat.**")
})
public class ApplicationConfig {

}
