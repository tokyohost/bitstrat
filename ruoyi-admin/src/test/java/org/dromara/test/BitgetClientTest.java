package org.dromara.test;

import com.alibaba.fastjson2.JSONObject;
import com.bitget.openapi.BitgetApiFacade;
import com.bitget.openapi.common.client.BitgetRestClient;
import com.bitget.openapi.common.domain.ClientParameter;
import com.bitget.openapi.dto.response.ResponseResult;
import com.bitstrat.config.BitgetClientService;
import com.bitstrat.constant.AccountTest;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.*;
import com.bitstrat.domain.bitget.CreateTpSlOnce;
import com.bitstrat.domain.bitget.UpdateTpSl;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/7 11:09
 * @Content
 */
@SpringBootTest
@Slf4j
public class BitgetClientTest {

    @Test
    public void testBigGetApi() throws IOException {

        String apiKey = "bg_43fc81f9a3da8caa2018390e5d900018";
        String secretKey = "2edb8676475540700815a95e76b19451f87504d16f91a7d42d80c3f1ecd39d7d";
        String passphrase = "Iloveyou520";
        String baseUrl = "https://api.bitget.com";

        BitgetRestClient client = BitgetClientService.createClient(apiKey, secretKey, passphrase, null,null);
//        HashMap<String, String> queryParams = new HashMap<>();
////        queryParams.put("coin", "USDT");
//        ResponseResult responseResult = client.bitget().v2().request().get("/api/v2/account/funding-assets", queryParams);
//        log.info(JSONObject.toJSONString(responseResult.getData()));
//        ResponseResult responseResultBot = client.bitget().v2().request().get("/api/v2/account/bot-assets", queryParams);
//        log.info(JSONObject.toJSONString(responseResultBot.getData()));
//        ResponseResult accounts = client.bitget().v1().mixAccount().getAccounts(Map.of("productType","umcbl"));
//        log.info(JSONObject.toJSONString(accounts.getData()));
//        ResponseResult account = client.bitget().v1().spotAccount().getInfo(queryParams);
//        log.info(JSONObject.toJSONString(account.getData()));
//
//        ClientParameter config = ClientParameter.builder().baseUrl(baseUrl).build();
//        BitgetRestClient build = BitgetRestClient.builder().configuration(config).build();
//        ResponseResult responseResult1 = build.bitget().v2().request().get("/api/v2/mix/market/current-fund-rate", Map.of("productType", "usdt-futures"
//            , "symbol", "BTCUSDT"));
//        log.info(JSONObject.toJSONString(responseResult1.getData()));

        //设置杠杆
        HashMap<String, String> params = new HashMap<>();
        params.put("symbol", "BTCUSDT");
        params.put("productType", "USDT-FUTURES");
        params.put("marginCoin", "USDT");
        params.put("leverage", "5");
        params.put("holdSide", "long");
        ResponseResult responseResult = client.bitget().v2().mixAccount().setLeverage(params);
        System.out.println(JSONObject.toJSONString(responseResult));

    }

    @Autowired
    ExchangeApiManager exchangeApiManager;
    @SneakyThrows
    @Test
    public void queryContractTpSlOrder() {
        Account bitgetTestAccount = AccountTest.getBitgetTestAccount();
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(ExchangeType.BITGET.getName());
        List<? extends TpSlOrder> tpSlOrders = exchangeService.queryContractTpSlOrder(bitgetTestAccount, "ETH");
        log.error("tpSlOrders: {}", JSONObject.toJSONString(tpSlOrders));


    }

    @SneakyThrows
    @Test
    public void updateContractTpSl() {
        Account bitgetTestAccount = AccountTest.getBitgetTestAccount();
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(ExchangeType.BITGET.getName());
        UpdateTpSl updateTpSl = new UpdateTpSl();
        updateTpSl.setOrderId("1366999037563310080");
        updateTpSl.setSymbol("ETH");
        updateTpSl.setTriggerType("mark_price");
        updateTpSl.setProductType("USDT-FUTURES");
        updateTpSl.setMarginCoin("USDT");
        updateTpSl.setTriggerPrice("3700");

        exchangeService.updateContractTpSl(bitgetTestAccount, "ETH",updateTpSl);


    }
    @SneakyThrows
    @Test
    public void createTpsl() {
        Account bitgetTestAccount = AccountTest.getBitgetTestAccount();
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(ExchangeType.BITGET.getName());
        CreateTpSlOnce updateTpSl = new CreateTpSlOnce();
        updateTpSl.setSymbol("ETH");
        updateTpSl.setProductType("USDT-FUTURES");
        updateTpSl.setMarginCoin("USDT");
        updateTpSl.setHoldSide("long");
        updateTpSl.setStopSurplusTriggerPrice("4400");
        updateTpSl.setStopSurplusTriggerType("mark_price");
        updateTpSl.setStopLossTriggerPrice("3600");
        updateTpSl.setStopLossTriggerType("mark_price");

        exchangeService.createTpSl(bitgetTestAccount, "ETH",updateTpSl);
    }
    @SneakyThrows
    @Test
    public void closePosition() {
        Account bitgetTestAccount = AccountTest.getBitgetTestAccount();
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(ExchangeType.BITGET.getName());
        OrderPosition orderPosition = new OrderPosition();
        orderPosition.setSymbol("ETH");
//        orderPosition.set
        exchangeService.closeContractPosition(bitgetTestAccount,orderPosition);
    }
    @SneakyThrows
    @Test
    public void getBalance() {
        Account bitgetTestAccount = AccountTest.getBitgetTestAccount();
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(ExchangeType.BITGET.getName());
//        orderPosition.set
        AccountBalance balance = exchangeService.getBalance(bitgetTestAccount, "USDT");
        log.info("balance: {}", JSONObject.toJSONString(balance));
    }

    @SneakyThrows
    @Test
    public void test() {
        BitgetApiFacade.BgNoAuthEndpoint noAuthEndpoint = BitgetApiFacade.noAuth(BitgetClientService.BASE_URL, 30L);
        ResponseResult responseResult = noAuthEndpoint.request().get("/api/v2/mix/market/contracts", Map.of("productType", "USDT-FUTURES"));
        log.info(JSONObject.toJSONString(responseResult));
    }
}
