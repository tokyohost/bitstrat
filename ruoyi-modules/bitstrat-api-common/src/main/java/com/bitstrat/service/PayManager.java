package com.bitstrat.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/14 15:37
 * @Content
 */
@Component
public class PayManager {
    HashMap<String, IPayService> payServiceHashMap = new HashMap<>();
    public PayManager(List<IPayService> payService) {
        for (IPayService iPayService : payService) {
            payServiceHashMap.put(iPayService.getPayType(), iPayService);
        }
    }

    public IPayService getPayService(String payType){
        return payServiceHashMap.get(payType);
    }
}
