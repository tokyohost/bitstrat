package com.bitstrat.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.bitstrat.gateway.config")
public class GatewayApplication {



    public static void main(String[] args) {
        MDC.put("hostIP", System.getenv("HOST_IP"));
		SpringApplication.run(GatewayApplication.class, args);
	}


}
