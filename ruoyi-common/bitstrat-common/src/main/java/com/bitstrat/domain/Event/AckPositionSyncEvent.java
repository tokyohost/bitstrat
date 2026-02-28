package com.bitstrat.domain.Event;

import com.bitstrat.domain.Account;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/6 18:55
 * @Content 主动同步仓位事件
 */

@Data
public class AckPositionSyncEvent {
    Account account;
    String exchangeName; //交易所名称
}
