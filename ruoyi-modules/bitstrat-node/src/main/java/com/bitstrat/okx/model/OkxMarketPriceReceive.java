package com.bitstrat.okx.model;

import lombok.Data;

import java.util.List;

@Data
public class OkxMarketPriceReceive {
    String event;
    SubscriptArg arg;
    List<OkxMarketPriceItem> data;

}
