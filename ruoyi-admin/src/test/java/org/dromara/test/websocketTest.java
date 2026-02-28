package org.dromara.test;

import com.bitstrat.domain.vo.CoinsTaskVo;
import com.bitstrat.strategy.impl.MAStrategy;
import com.bitstrat.wsClients.ExchangeConnectionManager;
import com.bitstrat.wsClients.constant.WebSocketType;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/2 15:41
 * @Content
 */
@Slf4j
@SpringBootTest
public class websocketTest {

    @Autowired
    ExchangeConnectionManager manager;

    @Test
    public void testSocket() throws Exception {
        // 创建连接
//        URI binanceUri = URI.create("wss://stream.binance.com:9443/ws/btcusdt@trade");
//        manager.createConnection("user1", "binance", binanceUri);

        URI bybiturl = URI.create("wss://stream.bybit.com/v5/public/linear");
        manager.createConnection("user2", "bybit", WebSocketType.LINER, bybiturl);

//        URI okxUri = URI.create("wss://ws.okx.com:8443/ws/v5/public");
//        manager.createConnection("user2", "okx", okxUri);

        // 获取Channel
        Channel binanceChannel = manager.getChannel("user1",null, "bybit",WebSocketType.LINER);
        if (binanceChannel != null) {
            binanceChannel.writeAndFlush(new TextWebSocketFrame("subscribe"));
        }


        // 关闭连接
//        manager.closeConnection("user1", "binance");
//        manager.closeAll();  // 关闭所有连接
//        manager.stop();  // 停止EventLoopGroup
    }
}
