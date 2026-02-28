package com.bitstrat.wsClients.msg.receive;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/28 20:39
 * @Content
 */

@Data
public class LinerReceiveMsg extends WsReceiveMsg{

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
