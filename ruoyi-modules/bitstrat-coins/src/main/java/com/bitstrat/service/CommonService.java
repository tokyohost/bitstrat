package com.bitstrat.service;

import org.dromara.common.core.domain.event.UserRegisterEvent;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/4 11:12
 * @Content
 */

@Service
public interface CommonService {

    public void userRegister(UserRegisterEvent userRegisterEvent);

    BigDecimal getRMB2USDRate();
}
