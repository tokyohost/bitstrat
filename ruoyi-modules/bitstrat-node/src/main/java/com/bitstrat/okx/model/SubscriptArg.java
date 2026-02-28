package com.bitstrat.okx.model;

import lombok.Data;

@Data
public class SubscriptArg {
    private String channel;
    private String instId;
    private String instType;
    private String instFamily;

}
