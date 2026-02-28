package com.bitstrat.domain.Event;

import com.bitstrat.domain.Account;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/6 20:18
 * @Content
 */

@Data
public class AckCreatePrivateWebsocketEvent {
    private Account account;
    private String exchangeName; //交易所名称

}
