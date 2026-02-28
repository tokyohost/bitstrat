package com.bitstrat.domain.Event;

import lombok.Data;

/**
 * 加载账户信息事件
 */
@Data
public class AckAccountLoadEvent {
    Long accountId;

}
