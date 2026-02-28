package com.bitstrat.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.bybit.ByBitAccount;
import com.bitstrat.domain.bybit.BybitSymbol;
import com.bitstrat.domain.bybit.BybitSymbolInfo;
import com.bitstrat.service.BybitService;
import com.bybit.api.client.domain.TradeOrderType;
import com.bybit.api.client.domain.asset.request.AssetDataRequest;
import com.bybit.api.client.domain.market.MarketStatus;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.domain.trade.PositionIdx;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.TimeInForce;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.restApi.BybitApiAsyncTradeRestClient;
import com.bybit.api.client.restApi.BybitApiCallback;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BybitServiceImpl implements BybitService {
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CommonServce commonServce;
    @Override
    public List<BybitSymbol> getSymbols() {
        ByBitAccount byBitAccount = commonServce.getBybitUserAccountByExchange(LoginHelper.getUserId());
        AssetDataRequest build = AssetDataRequest.builder().build();
        Object assetCoinInfo = BybitApiClientFactory.newInstance(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd()).newAssetRestClient().getAssetCoinInfo(build);

        String jsonString = JSONObject.toJSONString(assetCoinInfo);
        JSONObject data = JSONObject.parseObject(jsonString);
        if (data.getInteger("retCode") == 0) {
            List<BybitSymbol> bybitSymbols = JSONArray.parseArray(data.getJSONObject("result").getJSONArray("rows").toJSONString(), BybitSymbol.class);
            return bybitSymbols;
        }else{
            throw new RuntimeException(data.getString("retMsg"));
        }
//        ArrayList<BybitSymbol> bybitSymbols = new ArrayList<>();
//        List<BybitSymbolInfo> symbolsLiner = this.getSymbolsLiner();
//        for (BybitSymbolInfo bybitSymbolInfo : symbolsLiner) {
//            BybitSymbol bybitSymbol = new BybitSymbol();
//            bybitSymbol.setName(bybitSymbolInfo.getBaseCoin());
//            bybitSymbols.add(bybitSymbol);
//        }
//
//        return bybitSymbols;
    }
    @Override
    public List<BybitSymbolInfo> getSymbolsLiner() {
        return getSymbolsLiner(ExchangeType.BYBIT.getName());
    }

    public List<BybitSymbolInfo> getSymbolsLiner(String exchangeName) {
        if(ExchangeType.BYBIT.getName().equals(exchangeName)){
            ByBitAccount byBitAccount = commonServce.getBybitUserAccountByExchange(LoginHelper.getUserId());
            MarketDataRequest build = MarketDataRequest.builder().category(commonServce.getCateType()).marketStatus(MarketStatus.TRADING).build();
            BybitApiMarketRestClient bybitApiMarketRestClient = BybitApiClientFactory.newInstance(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd()).newMarketDataRestClient();
            Object assetCoinInfo = bybitApiMarketRestClient.getInstrumentsInfo(build);
            List<BybitSymbolInfo> all = new ArrayList<>();
            JSONObject data = JSONObject.from(assetCoinInfo);
            if (data.getInteger("retCode") == 0) {
                JSONObject result = data.getJSONObject("result");
                List<BybitSymbolInfo> bybitSymbols = JSONArray.parseArray(result.getJSONArray("list").toJSONString(), BybitSymbolInfo.class);
                List<BybitSymbolInfo> collected = bybitSymbols.stream().filter(item -> item.getQuoteCoin().equalsIgnoreCase("USDT")).collect(Collectors.toList());
                all.addAll(collected);
                if (result.containsKey("nextPageCursor")) {
                    //还有下一页
                    while (true) {
                        MarketDataRequest next = MarketDataRequest.builder()
                            .category(commonServce.getCateType())
                            .marketStatus(MarketStatus.TRADING)
                            .cursor(result.getString("nextPageCursor")).build();
                        Object instrumentsInfo = bybitApiMarketRestClient.getInstrumentsInfo(next);
                        JSONObject from = JSONObject.from(instrumentsInfo);
                        log.info("nextPageCursor ｛｝",result.getString("nextPageCursor"));
                        if (from.getInteger("retCode") == 0) {
                            result = from.getJSONObject("result");
                            bybitSymbols = JSONArray.parseArray(result.getJSONArray("list").toJSONString(), BybitSymbolInfo.class);
                            collected = bybitSymbols.stream().filter(item -> item.getQuoteCoin().equalsIgnoreCase("USDT")).collect(Collectors.toList());

                            all.addAll(collected);
                            if (StringUtils.isEmpty(result.getString("nextPageCursor"))) {
                                break;
                            }
                        }else{
                            break;
                        }
                    }

                }

                return all;
            }else{
                throw new RuntimeException(data.getString("retMsg"));
            }
        }
        return List.of();


//        ResponseEntity<String> response = restTemplate.getForEntity("https://api.bybit.com/spot/v1/symbols", String.class);
//
//        if (response.getStatusCode().is2xxSuccessful()) {
//            JSONObject data = JSONObject.parseObject(response.getBody());
//            if (data.getInteger("ret_code") == 0) {
//                List<BybitSymbol> bybitSymbols = JSONArray.parseArray(data.getJSONArray("result").toJSONString(), BybitSymbol.class);
//                return bybitSymbols;
//            }else{
//                throw new RuntimeException(data.getString("ret_msg"));
//            }
//
//
//        }else{
//            return new ArrayDeque<>();
//        }
    }


    /**
     * 下单
     * @param symbol
     * @param singleOrder
     * @param byBitAccount
     */
    @Override
    public String buy(String symbol, Double singleOrder, ByBitAccount byBitAccount) {
        BybitApiClientFactory factory = BybitApiClientFactory
            .newInstance(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd());
        CountDownLatch countDownLatch = new CountDownLatch(1);
        BybitApiAsyncTradeRestClient client = factory.newAsyncTradeRestClient();
        var newOrderRequest = TradeOrderRequest.builder().category(commonServce.getCateType()).symbol(symbol)
            .side(Side.BUY).orderType(TradeOrderType.MARKET).qty(String.valueOf(singleOrder)).timeInForce(TimeInForce.IMMEDIATE_OR_CANCEL)
            .positionIdx(PositionIdx.ONE_WAY_MODE).build();
        AtomicReference<String> orderId = new AtomicReference<>();
        AtomicReference<String> msg = new AtomicReference<>();
        client.createOrder(newOrderRequest, new BybitApiCallback() {
            @Override
            public void onResponse(Object response) {
                try{
                    log.info(response.toString());
                    JSONObject from = JSONObject.from(response);
                    if (from.getInteger("retCode") != 0){
                        from.getString("retMsg");
                        msg.set(from.getString("retMsg"));
                        throw new RuntimeException(from.getString("retMsg"));
                    }else{
                        orderId.set(from.getJSONObject("result").getString("orderId"));
                    }
                    log.info(response.toString());
                }finally {
                    countDownLatch.countDown();
                }


            }

            @Override
            public void onFailure(Throwable cause) {
                BybitApiCallback.super.onFailure(cause);
                log.info(cause.toString());
                msg.set(cause.getMessage());
                countDownLatch.countDown();
                throw new RuntimeException(String.valueOf(cause));
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(orderId.get() == null && StringUtils.isNotEmpty(msg.get())){
            throw new RuntimeException(msg.get());
        }
        return orderId.get();
    }

    @Override
    public String sell(String symbol, Double singleOrder, ByBitAccount byBitAccount) {
        BybitApiClientFactory factory = BybitApiClientFactory
            .newInstance(byBitAccount.getApiSecurity(), byBitAccount.getApiPwd());
        CountDownLatch countDownLatch = new CountDownLatch(1);
        BybitApiAsyncTradeRestClient client = factory.newAsyncTradeRestClient();
        var newOrderRequest = TradeOrderRequest.builder().category(commonServce.getCateType()).symbol(symbol)
            .side(Side.SELL).orderType(TradeOrderType.MARKET).qty(String.valueOf(singleOrder)).timeInForce(TimeInForce.IMMEDIATE_OR_CANCEL)
            .positionIdx(PositionIdx.ONE_WAY_MODE).build();
        AtomicReference<String> orderId = new AtomicReference<>();
        AtomicReference<String> msg = new AtomicReference<>();
        client.createOrder(newOrderRequest, new BybitApiCallback() {
            @Override
            public void onResponse(Object response) {
                try{
                    log.info(response.toString());
                    JSONObject from = JSONObject.from(response);
                    if (from.getInteger("retCode") != 0){
                        from.getString("retMsg");
                        msg.set(from.getString("retMsg"));
                        throw new RuntimeException(from.getString("retMsg"));
                    }else{
                        orderId.set(from.getJSONObject("result").getString("orderId"));
                    }
                    log.info(response.toString());
                }finally {
                    countDownLatch.countDown();
                }


            }

            @Override
            public void onFailure(Throwable cause) {
                BybitApiCallback.super.onFailure(cause);
                log.info(cause.toString());
                msg.set(cause.getMessage());
                countDownLatch.countDown();
                throw new RuntimeException(String.valueOf(cause));
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(orderId.get() == null && StringUtils.isNotEmpty(msg.get())){
            throw new RuntimeException(msg.get());
        }
        return orderId.get();
    }
}
