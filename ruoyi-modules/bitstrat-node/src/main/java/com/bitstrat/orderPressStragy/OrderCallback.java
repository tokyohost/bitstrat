package com.bitstrat.orderPressStragy;

import com.bitstrat.constant.OrderType;
import com.bitstrat.domain.OrderInfo;

@FunctionalInterface
public interface OrderCallback {
    void orderCallback(String orderType);
}
