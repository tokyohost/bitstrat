package com.bitstrat.store;

import com.bitstrat.domain.msg.ActiveLossPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/11 11:12
 * @Content
 */

@Component
public class RoleConfig {


    @Bean
    public RoleCenter getRoleCenter() {
        return new RoleCenter();
    }
}
