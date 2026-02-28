package com.bitstrat.store;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

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
