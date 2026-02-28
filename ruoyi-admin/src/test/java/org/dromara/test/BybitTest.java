package org.dromara.test;

import com.bitstrat.domain.CoinsTask;
import com.bitstrat.domain.vo.CoinsTaskVo;
import com.bitstrat.strategy.impl.MAStrategy;
import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.InstrumentStatus;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.service.BybitApiClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.log.event.OperLogEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Clock;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/2 15:41
 * @Content
 */
@Slf4j
//@SpringBootTest
public class BybitTest {

//    @Autowired
//    MAStrategy maStrategy;

    @Test
    public void testBybitApi() {
        log.info("testBybitApi start !!!!!");
        CoinsTaskVo coinsTask = new CoinsTaskVo();
        coinsTask.setSymbol("TRXUSDT");
        coinsTask.setInterval("1");
//        maStrategy.run(coinsTask,new OperLogEvent());

        log.info("testBybitApi end !!!!!");
    }
    @Test
    public void testBybitSymbols() {
        log.info("testBybitApi start !!!!!");
        CoinsTaskVo coinsTask = new CoinsTaskVo();
        coinsTask.setSymbol("TRXUSDT");
        coinsTask.setInterval("1");
//        maStrategy.run(coinsTask,new OperLogEvent());
        var client = BybitApiClientFactory.newInstance().newMarketDataRestClient();
        var instrumentInfoRequest = MarketDataRequest.builder().category(CategoryType.LINEAR).instrumentStatus(InstrumentStatus.TRADING).limit(500).build();
        Object instrumentsInfo = client.getInstrumentsInfo(instrumentInfoRequest);
        log.info(instrumentsInfo.toString());
        log.info("testBybitApi end !!!!!");
    }
    @Test
    public void testBybit2() {
        log.info("testBybitApi start !!!!!");
        Clock clock = Clock.systemDefaultZone();
        log.info("testBybitApi end !!!!!");
    }
}
