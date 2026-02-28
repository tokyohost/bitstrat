package com.bitstrat.wsClients.msg.receive;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/14 10:25
 * @Content  仓位强平警告！
 */

public class LiqWarning extends WsReceiveMsg {


    private  LiqWarning() {
    }

    public static LiqWarning fromWsReceiveMsg(WsReceiveMsg msg) {
        LiqWarning liqWarning = new LiqWarning();
        liqWarning.setEx(msg.getEx());
        liqWarning.setMsg(msg.getMsg());
        liqWarning.setUserId(msg.getUserId());
        liqWarning.setConnectionConfig(msg.getConnectionConfig());
        return liqWarning;
    }
}
