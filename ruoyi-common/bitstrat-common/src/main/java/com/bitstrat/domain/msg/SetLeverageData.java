package com.bitstrat.domain.msg;

import com.bitstrat.domain.server.MessageData;
import lombok.Data;

@Data
public class SetLeverageData implements MessageData {

    String category;
    String symbol;
    String buyLeverage;
    String sellLeverage;

}
