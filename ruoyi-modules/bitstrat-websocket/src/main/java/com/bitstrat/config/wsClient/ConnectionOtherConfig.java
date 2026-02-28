package com.bitstrat.config.wsClient;

import com.bitstrat.wsClients.domian.ConnectionConfig;
import com.bitstrat.wsClients.handler.WebSocketReconnectHandler;
import io.netty.channel.Channel;
import lombok.Data;

import static com.bitstrat.wsClients.constant.SocketConstant.RECONNECT_HANDLER_ATTRIBUTE_KEY;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/29 19:03
 * @Content
 */
@Data
public class ConnectionOtherConfig<T> {
    String clientId;
    String symbol;
    String exchange;
    String type;
    int side;
    private volatile Long dely = 0L;

    /**
     * 是否是服务器监听没有客户端
     *
     */
    boolean serverWatch = false;


    T ExtendConfig;

    ConnectionConfig connectionConfig;

    public void close() {
        if(connectionConfig != null) {
            Channel channel = connectionConfig.getChannel();
            WebSocketReconnectHandler reconnectHandler = channel.attr(RECONNECT_HANDLER_ATTRIBUTE_KEY).get();
            if(reconnectHandler != null) {
                reconnectHandler.close(channel);
            }else{
                channel.close();
            }
        }

    }
}
