package com.bitstrat.domain.Event;

import com.bitstrat.domain.abOrder.OrderTask;
import lombok.Data;

@Data
public class AckSendAndSaveABOrderLogEvent {

    OrderTask orderTask;
    String msg;
}
