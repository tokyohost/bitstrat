package com.bitstrat.domain.msg;

import lombok.Data;


public class PongMessage extends AbsMessage {

    public PongMessage() {
        this.type = "pong";
    }
}
