package com.bitstrat.orderPressStragy;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class OrderPressCenter {
    final HashMap<String, OrderPressStragy> map = new HashMap<>();
    public OrderPressCenter(List<OrderPressStragy> orderPressStragy) {
        for (OrderPressStragy stragy : orderPressStragy) {
            map.put(stragy.stragyName(), stragy);
        }

    }

    public OrderPressStragy getOrderPressStragy(String stragyName) {
        return map.get(stragyName);
    }
}
