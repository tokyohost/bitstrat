package com.bitstrat.domain;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.bitstrat.constant.WebsocketMsgType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data

public class PositionWebsocketMsgData<T> extends WebsocketMsgData<T> {

}
