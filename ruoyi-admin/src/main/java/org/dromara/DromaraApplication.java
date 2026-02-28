package org.dromara;

import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * 启动程序
 *
 * @author Lion Li
 */

@SpringBootApplication
@EnableDiscoveryClient
public class DromaraApplication {

    public static void main(String[] args) {
        // 读取 Docker 环境变量
        String hostIP = System.getenv("HOST_IP");
        if (hostIP != null) {
            MDC.put("hostIP", hostIP);
        }

        SpringApplication application = new SpringApplication(DromaraApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("=========================== Bitstrat Server Start Success ===========================");
    }

}
