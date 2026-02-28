package org.dromara.snailjob;

import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SnailJob Server 启动程序
 *
 * @author opensnail
 * @date 2024-05-17
 */
@SpringBootApplication
public class SnailJobServerApplication {

    public static void main(String[] args) {
        MDC.put("hostIP", System.getenv("HOST_IP"));
        SpringApplication.run(com.aizuda.snailjob.server.SnailJobServerApplication.class, args);
    }

}
