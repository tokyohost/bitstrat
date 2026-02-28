package com.bitstrat.wsClients.msg.receive;

import com.bitstrat.wsClients.domian.ConnectionConfig;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/28 17:05
 * @Content 收到的ws消息
 */


@Data
public class WsReceiveMsg {
    private ConnectionConfig connectionConfig;
    private String userId;
    private String ex;
    private String msg;

}
