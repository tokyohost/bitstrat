package com.bitstrat.wsClients.msg.receive;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/14 10:25
 * @Content  仓位被减仓警告！
 */

public class ADLWarning extends WsReceiveMsg {

    private ADLWarning() {
    }

    public static ADLWarning fromWsReceiveMsg(WsReceiveMsg msg) {
        ADLWarning adlWarning = new ADLWarning();
        adlWarning.setEx(msg.getEx());
        adlWarning.setMsg(msg.getMsg());
        adlWarning.setUserId(msg.getUserId());
        adlWarning.setConnectionConfig(msg.getConnectionConfig());
        return adlWarning;
    }
}
