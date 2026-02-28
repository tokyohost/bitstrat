package com.bitstrat.orderPressStragy;

import com.bitstrat.domain.msg.ActiveLossPoint;

public interface OrderPressStragy {

    public void pressOrder(String symbol,Double lastPrice,ActiveLossPoint activeLossPoint,OrderCallback orderCallback);

    public String stragyName();
}
