package com.bitstrat.strategy.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bitget.openapi.BitgetApiFacade;
import com.bitget.openapi.common.client.BitgetRestClient;
import com.bitget.openapi.dto.response.ResponseResult;
import com.bitstrat.annotation.AccountPaptrading;
import com.bitstrat.utils.APITypeHelper;
import com.bitstrat.config.BitgetClientService;
import com.bitstrat.constant.*;
import com.bitstrat.domain.*;
import com.bitstrat.domain.bitget.*;
import com.bitstrat.domain.vo.SymbolFundingRate;
import com.bitstrat.service.IBitgetApiService;
import com.bitstrat.store.OrderStatusConstant;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.BigDecimalUtils;
import com.bitstrat.wsClients.msg.BitgetWsMsg;
import com.bitstrat.wsClients.msg.SubscriptMsgs;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.common.core.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/7 11:43
 * @Content
 */
@Slf4j
@Service
@AccountPaptrading
public class BitgetExchangeRestServiceImpl implements ExchangeService {
    BitgetWsMsg bitgetWsMsg = new BitgetWsMsg();

    private final String SUCCESS = "00000";
    @Autowired
    IBitgetApiService bitgetApiService;

//    @Value("${bitget.paptrading}")
//    String paptrading;

    BitgetApiFacade.BgNoAuthEndpoint noAuthEndpoint = BitgetApiFacade.noAuth(BitgetClientService.BASE_URL, 30L);

    @Override
    public SubscriptMsgs getWsSubscriptMsgs() {

        return bitgetWsMsg;
    }

    @Override
    public String getExchangeName() {
        return ExchangeType.BITGET.getName();
    }

    @NotNull
    private static String checkSymbolLiner(String symbol) {
        if (!symbol.endsWith("_UMCBL")) {
            symbol = symbol + "_UMCBL";
        }
        if (symbol.endsWith("/UMCBL")) {
            symbol = symbol.replace("/", "_");
        }
        return symbol;
    }

    @NotNull
    private static String checkSymbolLinerV2(String symbol) {
//        if("1".equalsIgnoreCase(paptrading)){
//            //模拟盘是SETHSUSDT
//            if (!symbol.endsWith("SUSDT")) {
//                symbol = "S"+symbol + "SUSDT";
//            }
//            if (symbol.endsWith("/USDT")) {
//                symbol = symbol.replace("/", "_");
//            }
//        }else{
            if (!symbol.endsWith("USDT")) {
                symbol = symbol + "USDT";
            }
            if (symbol.endsWith("/USDT")) {
                symbol = symbol.replace("/", "_");
            }
//        }

        return symbol.toUpperCase();
    }
    private String deSymbolLinerV2(String symbol) {
        if (symbol.endsWith("USDT")) {
            symbol = symbol.replaceAll("USDT","");
        }
        if (symbol.endsWith("/USDT")) {
            symbol = symbol.replace("/USDT", "");
        }
        return symbol.toUpperCase();
    }

    @SneakyThrows
    @Override
    @Cacheable(value = "bitgetContractCoinInfo:cache#60s#60s#20", key = "'bitget'+':'+'ContractCoinInfo'+':'+#symbol",condition = "#symbol != null ")
    public CoinContractInfomation getContractCoinInfo(Account account, String symbol) {
        Map<String, String> params = Map.of("productType", "usdt-futures", "symbol", checkSymbolLinerV2(symbol));
        ResponseResult responseResult = noAuthEndpoint.request().get("/api/v2/mix/market/contracts", params);
        if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {
            Object data = responseResult.getData();
            JSONArray from = JSONArray.from(data);
            for (Object item : from) {
                UmcblItemV2 umcblItemV2 = JSONObject.from(item).to(UmcblItemV2.class);
                CoinContractInfomation coinContractInfomation = new CoinContractInfomation();
                coinContractInfomation.setSymbol(symbol);
                coinContractInfomation.setSizeMultiplier(umcblItemV2.getSizeMultiplier());
                coinContractInfomation.setStep(umcblItemV2.getSizeMultiplier());
                coinContractInfomation.setCalcPlaces(umcblItemV2.getVolumePlace());
                coinContractInfomation.setMinTradeUSDT(umcblItemV2.getMinTradeUSDT());
                coinContractInfomation.setMinSz(umcblItemV2.getMinTradeNum());
                coinContractInfomation.setCtMult(umcblItemV2.getSizeMultiplier());
                coinContractInfomation.setContractValue(umcblItemV2.getMinTradeNum());
                coinContractInfomation.setMinLeverage(umcblItemV2.getMinLever());
                coinContractInfomation.setMaxLeverage(umcblItemV2.getMaxLever());
                coinContractInfomation.setMaxLmtSz(BigDecimal.valueOf(Integer.MAX_VALUE));
                coinContractInfomation.setPriceEndStep(umcblItemV2.getPriceEndStep());
                coinContractInfomation.setPricePlace(umcblItemV2.getPricePlace());
                return coinContractInfomation;
            }

        } else {
            throw new RuntimeException(responseResult.getMsg());
        }
        return null;
    }

    @SneakyThrows
    @Override
    public String setLeverage(Account account, Integer leverage, String symbol, String side) {
        HashMap<String, String> params = new HashMap<>();
        params.put("symbol", checkSymbolLinerV2(symbol));
        params.put("productType", "USDT-FUTURES");
        params.put("marginCoin", "USDT");
        params.put("leverage", leverage + "");
        params.put("holdSide", side);
        BitgetRestClient client = getClient(account, null);
        ResponseResult responseResult = client.bitget().v2().mixAccount().setLeverage(params);
        if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {
            //请求成功
            return CorssLeverageStatus.SUCCESS;
        }
        throw new RuntimeException(responseResult.getMsg());
    }

    private BitgetRestClient getClient(Account account, String signType) {
        String paptrading = getPaptrading();
        BitgetRestClient client = BitgetClientService.createClient(account.getApiKey(), account.getApiSecret(), account.getPassphrase(), signType,paptrading);
        return client;
    }

    @NotNull
    private static String getPaptrading() {
        String paptrading = "1";
        String type = APITypeHelper.peek();
        if(StringUtils.isBlank(type)){
            return paptrading;
        }
        switch (type) {
            case ApiTypeConstant.TEST:
                paptrading = "1";
                break;
            case ApiTypeConstant.PRO:
                paptrading = "0";
                break;
        }
        return paptrading;
    }

    @SneakyThrows
    @Override
    public boolean checkApi(Account account) {
        BitgetRestClient client = getClient(account, null);
        ResponseResult accounts = client.bitget().v2().mixAccount().getAccounts(Map.of("productType", "USDT-FUTURES"));
        if (accounts.getCode().equalsIgnoreCase(SUCCESS)) {
            //请求成功
            return true;
        }
        return false;
    }

    @SneakyThrows
    @Override
    public OrderOptStatus buyContract(Account account, OrderVo params) {
        BitgetRestClient client = getClient(account, null);
//        BitgetRestClient client = BitgetClientService.createClient(account.getApiKey(), account.getApiSecret(), account.getPassphrase(), null);
        HashMap<String, String> orderParams = new HashMap<>();
        orderParams.put("symbol", checkSymbolLinerV2(params.getSymbol()));
        orderParams.put("productType", "USDT-FUTURES");
        /**
         * 仓位模式
         * isolated: 逐仓
         * crossed: 全仓
         */
        orderParams.put("marginMode", "crossed");
        orderParams.put("marginCoin", "USDT");
        orderParams.put("size", params.getSize().toPlainString());

        /**
         * 交易方向
         * buy: 单向持仓时代表买入，双向持仓时代表多头方向
         * sell: 单向持仓时代表卖出，双向持仓时代表空头方向
         */
        orderParams.put("side", "buy");

        //是否只减仓
        if (Objects.nonNull(params.getReduceOnly()) && params.getReduceOnly()) {
            /**
             * 只减仓(仅适用单向持仓模式下)
             * YES
             * NO(默认)
             */
            orderParams.put("reduceOnly", "YES");
            //平多规则为：side=buy,tradeSide=close；平空规则为：side=sell,tradeSide=close
            orderParams.put("side", "sell");
            /**
             * 交易类型(仅限双向持仓)
             * 双向持仓模式下必填，单向持仓时不要填，否则会报错
             * open: 开仓
             * close: 平仓
             */
            orderParams.put("tradeSide", "close");
        } else {
            orderParams.put("tradeSide", "open");
        }

        if (OrderType.MARKET.equalsIgnoreCase(params.getOrderType())) {
            orderParams.put("orderType", OrderType.MARKET.toLowerCase());

        } else if (OrderType.LIMIT.equalsIgnoreCase(params.getOrderType())) {
            orderParams.put("orderType", OrderType.LIMIT.toLowerCase());
            orderParams.put("price", params.getPrice().toPlainString());
        }
        /**
         * 订单有效期
         * ioc: 无法立即成交的部分就撤销
         * fok: 无法全部立即成交就撤销
         * gtc: 普通订单, 订单会一直有效，直到被成交或者取消
         * post_only: 只做maker
         * "orderType"为limit限价单时必填，若省略则默认为gtc
         */
        orderParams.put("force", "gtc");
        long clientOid = IdUtil.getSnowflake().nextId();
        orderParams.put("clientOid", clientOid + "");
        ResponseResult responseResult = client.bitget().v2().mixOrder().placeOrder(orderParams);
        log.info("bitget 创建订单：{}", responseResult.getMsg());
        if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {
            //请求成功
            Object data = responseResult.getData();
            JSONObject orderResult = JSONObject.from(data);
            OrderOptStatus orderOptStatus = new OrderOptStatus();
            orderOptStatus.setOrderId(orderResult.getString("clientOid"));
            orderOptStatus.setSymbol(params.getSymbol());
            orderOptStatus.setStatus(CrossOrderStatus.SUCCESS);
            return orderOptStatus;
        }
        throw new RuntimeException("创建订单失败 " + responseResult.getMsg());
    }

    @SneakyThrows
    @Override
    public OrderOptStatus sellContract(Account account, OrderVo params) {
        BitgetRestClient client = getClient(account, null);
        HashMap<String, String> orderParams = new HashMap<>();
        orderParams.put("symbol", checkSymbolLinerV2(params.getSymbol()));
        orderParams.put("productType", "USDT-FUTURES");
        /**
         * 仓位模式
         * isolated: 逐仓
         * crossed: 全仓
         */
        orderParams.put("marginMode", "crossed");
        orderParams.put("marginCoin", "USDT");
        orderParams.put("size", params.getSize().toPlainString());

        /**
         * 交易方向
         * buy: 单向持仓时代表买入，双向持仓时代表多头方向
         * sell: 单向持仓时代表卖出，双向持仓时代表空头方向
         */
        orderParams.put("side", "sell");

        //是否只减仓
        if (Objects.nonNull(params.getReduceOnly()) && params.getReduceOnly()) {
            /**
             * 只减仓(仅适用单向持仓模式下)
             * YES
             * NO(默认)
             */
            orderParams.put("reduceOnly", "YES");

            //平多规则为：side=buy,tradeSide=close；平空规则为：side=sell,tradeSide=close
            orderParams.put("side", "buy");
            /**
             * 交易类型(仅限双向持仓)
             * 双向持仓模式下必填，单向持仓时不要填，否则会报错
             * open: 开仓
             * close: 平仓
             */
            orderParams.put("tradeSide", "close");
        } else {
            orderParams.put("tradeSide", "open");
        }

        if (OrderType.MARKET.equalsIgnoreCase(params.getOrderType())) {

            orderParams.put("orderType", OrderType.MARKET.toLowerCase());
        } else if (OrderType.LIMIT.equalsIgnoreCase(params.getOrderType())) {
            orderParams.put("price", params.getPrice().toPlainString());
            orderParams.put("orderType", OrderType.LIMIT.toLowerCase());
        }
        /**
         * 订单有效期
         * ioc: 无法立即成交的部分就撤销
         * fok: 无法全部立即成交就撤销
         * gtc: 普通订单, 订单会一直有效，直到被成交或者取消
         * post_only: 只做maker
         * "orderType"为limit限价单时必填，若省略则默认为gtc
         */
        orderParams.put("force", "gtc");
        long clientOid = IdUtil.getSnowflake().nextId();
        orderParams.put("clientOid", clientOid + "");
        ResponseResult responseResult = client.bitget().v2().mixOrder().placeOrder(orderParams);
        log.info("bitget 创建订单：{}", responseResult.getMsg());
        if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {
            //请求成功
            Object data = responseResult.getData();
            JSONObject orderResult = JSONObject.from(data);
            OrderOptStatus orderOptStatus = new OrderOptStatus();
            orderOptStatus.setOrderId(orderResult.getString("clientOid"));
            orderOptStatus.setSymbol(params.getSymbol());
            orderOptStatus.setStatus(CrossOrderStatus.SUCCESS);
            return orderOptStatus;
        }
        throw new RuntimeException("bitget 创建订单失败 " + responseResult.getMsg());
    }

    @Override
    public String buySpot(Account account, JSONObject params) {
        return "";
    }

    @Override
    public String sellSpot(Account account, JSONObject params) {
        return "";
    }

    @Override
    public String checkContractOrder(Account account, OrderInfo orderInfo) {
        return "";
    }

    @Override
    public void checkSpotOrder(Account account, JSONObject params) {

    }

    @Override
    public void cancelSpotOrder(Account account, OrderInfo params) {

    }

    @SneakyThrows
    @Override
    public String cancelContractOrder(Account account, OrderOptStatus order) {
        BitgetRestClient client = getClient(account, null);
        HashMap<String, String> cancelParams = new HashMap<>();
        cancelParams.put("symbol", checkSymbolLinerV2(order.getSymbol()));
        cancelParams.put("productType", "USDT-FUTURES");
        cancelParams.put("marginCoin", "USDT");
        cancelParams.put("clientOid", order.getOrderId());
        ResponseResult responseResult = client.bitget().v2().mixOrder().cancelOrder(cancelParams);
        if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {
            //请求成功
            return CrossOrderStatus.SUCCESS;
        }
        log.error("bitget 取消订单报错 {}", responseResult.getMsg());
        return CrossOrderStatus.FAIL;
    }

    @SneakyThrows
    @Override
    public OrderCloseResult closeContractPosition(Account account, OrderPosition order) {
        String paptrading = getPaptrading();
        BitgetRestClient client = getClient(account, null);
        HashMap<String, String> queryParams = new HashMap<>();
        /**
         * 产品类型
         * USDT-FUTURES USDT专业合约
         * COIN-FUTURES 混合合约
         * USDC-FUTURES USDC专业合约
         */
        queryParams.put("productType", "USDT-FUTURES");
        queryParams.put("symbol", checkSymbolLinerV2(order.getSymbol()));
        queryParams.put("marginCoin", "USDT");
        ResponseResult responseResult = client.bitget().v2().mixOrder().followerClosePositions(queryParams,"1".equalsIgnoreCase(paptrading) ? Map.of("paptrading","1"):Map.of());
        if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {
            OrderCloseResult result = new OrderCloseResult();
            result.setStatus(CrossOrderStatus.SUCCESS);
            result.setMsg("ok");

            return result;
        }
        OrderCloseResult result = new OrderCloseResult();
        result.setStatus(CrossOrderStatus.FAIL);
        result.setMsg(responseResult.getMsg());

        return result;
    }

    @SneakyThrows
    @Override
    public OrderPosition queryContractPosition(Account account, String symbol, PositionParams params) {
        BitgetRestClient client = getClient(account, null);
        HashMap<String, String> queryParams = new HashMap<>();
        /**
         * 产品类型
         * USDT-FUTURES USDT专业合约
         * COIN-FUTURES 混合合约
         * USDC-FUTURES USDC专业合约
         */
        queryParams.put("productType", "USDT-FUTURES");
        queryParams.put("symbol", checkSymbolLinerV2(symbol));
        queryParams.put("marginCoin", "USDT");

        ResponseResult responseResult = client.bitget().v2().mixAccount().singlePosition(queryParams);
        if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {
            //请求成功
            Object data = responseResult.getData();
            JSONArray from = JSONArray.from(data);
            for (Object positionItem : from) {
                BitgetPositionDetail bitgetPositionDetail = JSONObject.from(positionItem).to(BitgetPositionDetail.class);
                OrderPosition orderPosition = new OrderPosition();
                orderPosition.setSize(bitgetPositionDetail.getTotal().abs());

                orderPosition.setFee(bitgetPositionDetail.getDeductedFee().negate());
                orderPosition.setLever(bitgetPositionDetail.getLeverage());
                orderPosition.setEx(ExchangeType.BITGET.getName());
                orderPosition.setSymbol(symbol);
                orderPosition.setFundingFee(bitgetPositionDetail.getTotalFee() == null ? BigDecimal.ZERO : bitgetPositionDetail.getTotalFee());
                orderPosition.setMgnRatio(bitgetPositionDetail.getMarginRatio());
                orderPosition.setRealizedPnl(bitgetPositionDetail.getAchievedProfits());
                orderPosition.setProfit(bitgetPositionDetail.getUnrealizedPL());
                orderPosition.setSettledPnl(bitgetPositionDetail.getAchievedProfits());
                orderPosition.setAvgPrice(bitgetPositionDetail.getOpenPriceAvg());
                orderPosition.setLiqPx(bitgetPositionDetail.getLiquidationPrice());
                /**
                 * 持仓方向
                 * long: 多仓
                 * short: 空仓
                 */
                orderPosition.setSide(bitgetPositionDetail.getHoldSide().toLowerCase());
                orderPosition.setAccountId(account.getId());
                return orderPosition;
            }
        }
        return null;
    }

    @SneakyThrows
    @Override
    public BigDecimal queryClosePositionProfit(Account account, String symbol, PositionParams params) {
        long endTime = System.currentTimeMillis();
        BigDecimal total = new BigDecimal(0);
        //查询这个币种开仓到平仓中间所有历史仓位的盈亏累加起来
        BitgetRestClient client = getClient(account, null);
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("symbol", checkSymbolLinerV2(symbol));
        queryParams.put("productType", "USDT-FUTURES");
        queryParams.put("startTime", params.getStartTime() + "");
        queryParams.put("endTime", endTime + "");
        queryParams.put("limit", "100");
        while (true) {
            //可能有分页
            ResponseResult responseResult = client.bitget().v2().request().get("/api/v2/mix/position/history-position", queryParams);
            log.info("查询历史持仓返回 {}", JSONObject.toJSONString(responseResult));
            if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {
                Object data = responseResult.getData();
                JSONObject dataObj = JSONObject.from(data);
                String endId = dataObj.getString("endId");
                JSONArray list = dataObj.getJSONArray("list");
                List<BitgetHistoryPositionItem> positionLists = list.toList(BitgetHistoryPositionItem.class);


                if (StringUtils.isEmpty(endId) || CollectionUtils.isEmpty(positionLists)
                    || positionLists.size() < 100) {
                    //翻页完成
                    break;
                }
                for (BitgetHistoryPositionItem position : positionLists) {
                    if (position.getSymbol().equalsIgnoreCase(checkSymbolLinerV2(symbol))) {
                        total = total.add(position.getNetProfit());
                    }
                }
                queryParams.put("idLessThan", endId);
                log.info("bitget 查询仓位盈亏继续翻页 当前页码{}",endId);

            }

        }

        return total;
    }

    @SneakyThrows
    @Override
    public SymbolFundingRate getSymbolFundingRate(String symbol) {

        CompletableFuture<FundingTimeItem> fundingTimeFuture = CompletableFuture.supplyAsync(() -> {
                HashMap<String, String> params = new HashMap<>();
                params.put("symbol", checkSymbolLinerV2(symbol));
                params.put("productType", "usdt-futures");
                ResponseResult responseResult = null;
                try {
                    responseResult = noAuthEndpoint.request().get("/api/v2/mix/market/funding-time", params);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {
                    /**
                     * {
                     *             "symbol": "BTCUSDT",
                     *             "nextFundingTime": "1746633600000",
                     *             "ratePeriod": "8"
                     *         }
                     */
                    Object data = responseResult.getData();
                    JSONArray from = JSONArray.from(data);
                    for (Object item : from) {
                        FundingTimeItem fundingTimeItem = JSONObject.from(item).to(FundingTimeItem.class);
                        return fundingTimeItem;
                    }

                }
                return null;
            }, bitgetApiService.getBitgetExecutorService())
            .exceptionally((e) -> {
                log.error("获取资金费时间出错", e);
                return null;
            });


        CompletableFuture<FundingFeeItem> fundingFeeFuture = CompletableFuture.supplyAsync(() -> {
                HashMap<String, String> params = new HashMap<>();
                params.put("symbol", checkSymbolLinerV2(symbol));
                params.put("productType", "usdt-futures");
                ResponseResult responseResult = null;
                try {
                    responseResult = noAuthEndpoint.request().get("/api/v2/mix/market/current-fund-rate", params);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {
                    /**
                     * {
                     *             "symbol": "BTCUSDT",
                     *             "nextFundingTime": "1746633600000",
                     *             "ratePeriod": "8"
                     *         }
                     */
                    Object data = responseResult.getData();
                    JSONArray from = JSONArray.from(data);
                    for (Object item : from) {
                        FundingFeeItem fundingFeeItem = JSONObject.from(item).to(FundingFeeItem.class);
                        return fundingFeeItem;
                    }

                }
                return null;
            }, bitgetApiService.getBitgetExecutorService())
            .exceptionally((e) -> {
                log.error("获取资金费时间出错", e);
                return null;
            });

        SymbolFundingRate symbolFundingRate = new SymbolFundingRate();
        FundingTimeItem fundingTimeItem = fundingTimeFuture.join();
        if (fundingTimeItem != null) {
            symbolFundingRate.setNextFundingTime(fundingTimeItem.getNextFundingTime());
        }
        FundingFeeItem fundingFeeItem = fundingFeeFuture.join();
        if (fundingFeeItem != null) {
            symbolFundingRate.setFundingRate(fundingFeeItem.getFundingRate());
        }

        return symbolFundingRate;
    }

    /**
     * [{"marginCoin":"USDT","locked":"0","available":"100.04770401","crossMaxAvailable":"100.04770401","fixedMaxAvailable":"100.04770401","maxTransferOut":"100.04770401","equity":"100.04770401","usdtEquity":"100.04770401","btcEquity":"0.001036970064","crossRiskRate":"0","unrealizedPL":"0","bonus":"0"}]
     *
     * @param account
     * @param coin
     * @return
     */
    @SneakyThrows
    @Override
    public AccountBalance getBalance(Account account, String coin) {
        BitgetRestClient client = getClient(account, null);
        ResponseResult accounts = client.bitget().v2().mixAccount().getAccounts(Map.of("productType", "USDT-FUTURES"));
        AccountBalance accountBalance = new AccountBalance();
        if (accounts.getCode().equalsIgnoreCase(SUCCESS)) {
            JSONArray from = JSONArray.from(accounts.getData());
            for (Object info : from) {
                JSONObject coinItem = JSONObject.from(info);
                //保证金币种
                if (coinItem.getString("marginCoin").equalsIgnoreCase(coin)) {
                    accountBalance.setSymbol(coinItem.getString("marginCoin"));
                    accountBalance.setBalance(coinItem.getBigDecimal("available"));
                    accountBalance.setEquity(coinItem.getBigDecimal("usdtEquity"));
                    accountBalance.setFreeBalance(coinItem.getBigDecimal("crossedMaxAvailable"));

                    accountBalance.setApiId(account.getId());
                    accountBalance.setApiName(account.getName());
                    return accountBalance;
                }
            }
        }
        accountBalance.setApiId(account.getId());
        accountBalance.setApiName(account.getName());
        return accountBalance;
    }

    @SneakyThrows
    @Override
//    @Cacheable(value = "bitgetFee:cache#30s#60s#20", key = "'bitget'+':'+'fee'+':'+#account.apiKey",condition = "#coin != null ")
    public SymbolFee getFee(Account account, String coin) {
        BitgetRestClient client = getClient(account, null);


        CompletableFuture<SymbolFee> linerFuture = CompletableFuture.supplyAsync(() -> {
            HashMap<String, String> params = new HashMap<>();
            /**
             * {@link https://www.bitget.com/zh-CN/api-doc/contract/market/Get-All-Symbols-Contracts}
             */
            params.put("productType", "usdt-futures");
            params.put("symbol", checkSymbolLinerV2(coin));
            ResponseResult contracts = null;
            try {
                contracts = client.bitget().v2().mixMarket().contracts(params);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (contracts.getCode().equalsIgnoreCase(SUCCESS)) {
                Object data = contracts.getData();
                JSONArray from = JSONArray.from(data);
                for (Object info : from) {
                    UmcblItemV2 umcblItemV2 = JSONObject.from(info).to(UmcblItemV2.class);
                    SymbolFee symbolFee = new SymbolFee();
                    symbolFee.setLinerTakerFeeRate(umcblItemV2.getTakerFeeRate());
                    symbolFee.setLinerMakerFeeRate(umcblItemV2.getMakerFeeRate());
                    return symbolFee;

                }
            }
            return null;
        }, bitgetApiService.getBitgetExecutorService()).exceptionally((e) -> {
            e.printStackTrace();
            log.error("获取bitget liner手续费率失败", e);
            return null;
        });

        CompletableFuture<SymbolFee> spotFuture = CompletableFuture.supplyAsync(() -> {
            HashMap<String, String> params = new HashMap<>();
            /**
             * {@link https://www.bitget.com/zh-CN/api-doc/spot/market/Get-Symbols}
             */
            params.put("symbol", checkSymbolLinerV2(coin));
            ResponseResult contracts = null;
            try {
                contracts = client.bitget().v2().spotMarket().symbols(params);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (contracts.getCode().equalsIgnoreCase(SUCCESS)) {
                Object data = contracts.getData();
                JSONArray from = JSONArray.from(data);
                for (Object info : from) {
                    SpotItemV2 spotItemV2 = JSONObject.from(info).to(SpotItemV2.class);
                    SymbolFee symbolFee = new SymbolFee();
                    symbolFee.setSportTakerFeeRate(spotItemV2.getTakerFeeRate());
                    symbolFee.setSportMakerFeeRate(spotItemV2.getMakerFeeRate());
                    return symbolFee;

                }
            }
            return null;
        }, bitgetApiService.getBitgetExecutorService()).exceptionally((e) -> {
//            e.printStackTrace();
            log.error("获取bitget spot 手续费率失败", e);
            return null;
        });
        SymbolFee result = new SymbolFee();
        SymbolFee linerFee = linerFuture.join();
        if (Objects.nonNull(linerFee)) {
            result.setLinerMakerFeeRate(linerFee.getLinerMakerFeeRate());
            result.setLinerTakerFeeRate(linerFee.getLinerTakerFeeRate());
        }
        SymbolFee spotFee = spotFuture.join();
        if (Objects.nonNull(spotFee)) {
            result.setSportMakerFeeRate(spotFee.getSportMakerFeeRate());
            result.setSportTakerFeeRate(spotFee.getSportTakerFeeRate());
        }
        return result;
    }

    @SneakyThrows
    @Override
    public BigDecimal getNowPrice(Account account, String symbol) {
        Map<String, String> params = Map.of("symbol", checkSymbolLinerV2(symbol), "productType", "USDT-FUTURES");
        ResponseResult responseResult = noAuthEndpoint.request().get("/api/v2/mix/market/ticker", params);
        if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {
            Object data = responseResult.getData();
            JSONArray from = JSONArray.from(data);
            for (Object info : from) {
                TickerItem tickerItem = JSONObject.from(info).to(TickerItem.class);
                if (tickerItem.getSymbol().equalsIgnoreCase(checkSymbolLinerV2(symbol))) {
                    return tickerItem.getLastPr();
                }
            }
        }

        return null;
    }

    public static void main(String[] args) throws IOException {
        BitgetApiFacade.BgNoAuthEndpoint noAuthEndpoint = BitgetApiFacade.noAuth(BitgetClientService.BASE_URL, 30L);
        Map<String, String> params = Map.of("symbol", checkSymbolLinerV2("BTC"), "productType", "USDT-FUTURES");
        ResponseResult responseResult = noAuthEndpoint.request().get("/api/v2/mix/market/ticker", params);
        System.out.printf(responseResult.getData().toString());
    }
    @SneakyThrows
    @Override
    public TickerItem getNowPrice(Account account, String symbol,String bitgetOnly) {
        Map<String, String> params = Map.of("symbol", checkSymbolLinerV2(symbol), "productType", "USDT-FUTURES");
        ResponseResult responseResult = noAuthEndpoint.request().get("/api/v2/mix/market/ticker", params);
        if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {
            Object data = responseResult.getData();
            JSONArray from = JSONArray.from(data);
            for (Object info : from) {
                TickerItem tickerItem = JSONObject.from(info).to(TickerItem.class);
                if (tickerItem.getSymbol().equalsIgnoreCase(checkSymbolLinerV2(symbol))) {
                    return tickerItem;
                }
            }
        }

        return null;
    }

    @Override
    public OrderVo calcOrderSize(OrderVo longOrder) {
        return longOrder;
    }

    @Override
    public BigDecimal calcOrderSize(String symbol, BigDecimal size) {
        return size;
    }

    @Override
    public BigDecimal calcShowSize(String symbol, BigDecimal size) {
        return size;
    }

    @SneakyThrows
    @Override
    public OrderOptStatus queryContractOrderStatus(Account account, OrderOptStatus orderId) {
        BitgetRestClient client = getClient(account, null);
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("symbol", checkSymbolLinerV2(orderId.getSymbol()));
        queryParams.put("productType", "USDT-FUTURES");
        queryParams.put("clientOid", orderId.getOrderId());
        ResponseResult responseResult = client.bitget().v2().request().get("/api/v2/mix/order/detail", queryParams);
        List<String> bitgetProcessStatus = OrderStatusConstant.bitgetProcessStatus;
        List<String> bitgetEndStatus = OrderStatusConstant.bitgetEndStatus;


        if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {
            //请求成功
            Object data = responseResult.getData();
            BitgetOrderDetail bitgetOrderDetail = JSONObject.from(data).to(BitgetOrderDetail.class);
            if (bitgetProcessStatus.contains(bitgetOrderDetail.getState().toLowerCase())) {
                //中间态
                OrderOptStatus orderOptStatus = new OrderOptStatus();
                orderOptStatus.setOrderId(bitgetOrderDetail.getClientOid());

                orderOptStatus.setStatus(CrossOrderStatus.PROCESS);
                return orderOptStatus;
            } else if (bitgetEndStatus.contains(bitgetOrderDetail.getState().toLowerCase())) {
                //结束态
                OrderOptStatus orderOptStatus = new OrderOptStatus();
                orderOptStatus.setOrderId(bitgetOrderDetail.getClientOid());

                orderOptStatus.setStatus(CrossOrderStatus.END);
                return orderOptStatus;
            }
        }
        OrderOptStatus orderOptStatus = new OrderOptStatus();
        orderOptStatus.setOrderId(orderId.getOrderId());
        orderOptStatus.setStatus(CrossOrderStatus.UNKNOW);
        return orderOptStatus;
    }

    @SneakyThrows
    @Override
    public List<ContractOrder> queryContractOrdersByIds(Account account, List<String> orderIds, String symbol) {
        BitgetRestClient client = getClient(account, null);

        List<String> bitgetProcessStatus = OrderStatusConstant.bitgetProcessStatus;
        List<String> bitgetEndStatus = OrderStatusConstant.bitgetEndStatus;

        List<ContractOrder> orders = new ArrayList<>();
        for (String orderId : orderIds) {
            HashMap<String, String> queryParams = new HashMap<>();
            queryParams.put("symbol", checkSymbolLinerV2(symbol));
            queryParams.put("productType", "USDT-FUTURES");
            queryParams.put("clientOid", orderId);
            ResponseResult responseResult = client.bitget().v2().request().get("/api/v2/mix/order/detail", queryParams);
            log.info("bitget 查询订单结果 {}", JSONObject.toJSONString(responseResult));

            if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {
                Object data = responseResult.getData();
                BitgetOrderDetail bitgetOrderDetail = JSONObject.from(data).to(BitgetOrderDetail.class);
                String orderStatus = bitgetOrderDetail.getState();

                ContractOrder contractOrder = new ContractOrder();
                contractOrder.setOrderId(orderId);
                contractOrder.setFee(bitgetOrderDetail.getFee());
                contractOrder.setAvgPrice(bitgetOrderDetail.getPriceAvg());
                contractOrder.setEx(ExchangeType.BYBIT.getName());
                contractOrder.setSize(bitgetOrderDetail.getSize());
//                        String side = ordered.getString("side");
//                        if (StringUtils.isNotEmpty(side)) {
//                            contractOrder.setSide("Buy".equalsIgnoreCase(side) ? SideType.LONG : SideType.SHORT);
//                        }
                if (OrderType.MARKET.equalsIgnoreCase(bitgetOrderDetail.getOrderType())) {
                    bitgetOrderDetail.setPrice(bitgetOrderDetail.getPriceAvg());
                }
                contractOrder.setPrice(bitgetOrderDetail.getPrice());
                contractOrder.setStatus(orderStatus.toLowerCase());
                BigDecimal leavesQty = bitgetOrderDetail.getSize().subtract(bitgetOrderDetail.getBaseVolume());
                contractOrder.setLeavesQty(leavesQty);
                contractOrder.setLeavesValue(leavesQty.multiply(bitgetOrderDetail.getPrice()));
                contractOrder.setCumExecQty(bitgetOrderDetail.getBaseVolume());
                contractOrder.setCumExecValue(bitgetOrderDetail.getBaseVolume().multiply(bitgetOrderDetail.getPrice()));

                if (bitgetProcessStatus.contains(orderStatus)) {
                    //中间状态，还没成交
                    contractOrder.setOrderEnd(false);
                } else if (bitgetEndStatus.contains(orderStatus)) {
                    //最终态
                    contractOrder.setOrderEnd(true);
                }
                orders.add(contractOrder);
            } else {
                log.error("bitget 查询订单出错：{}", JSONObject.toJSONString(responseResult));
            }

        }

        return orders;
    }

    @SneakyThrows
    @Override
    public synchronized OrderVo updateContractOrder(Account account, OrderVo vo) {
        BitgetRestClient client = getClient(account, null);
        long newOrderId = IdUtil.getSnowflake().nextId();
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("symbol", checkSymbolLinerV2(vo.getSymbol()));
        queryParams.put("productType", "USDT-FUTURES");
        queryParams.put("clientOid", vo.getOrderId());
        queryParams.put("newClientOid", newOrderId + "");
        if (Objects.nonNull(vo.getPrice())) {
            //新价格
            queryParams.put("newPrice", vo.getPrice().toPlainString());
        }

        if (Objects.nonNull(vo.getOrderSize())) {
            //新数量
            queryParams.put("newSize", vo.getOrderSize().toPlainString());
        }
//        vo.setOrderId(newOrderId + "");
        ResponseResult responseResult = client.bitget().v2().request().post("/api/v2/mix/order/modify-order", queryParams);
        /**
         * {
         *     "code": SUCCESS,
         *     "data": {
         *         "orderId": "121212121212",
         *         "clientOid": "BITGET#1627293504612"
         *     },
         *     "msg": "success",
         *     "requestTime": 1627293504612
         * }
         */
        log.info("修改订单结果 {}",JSONObject.toJSONString(responseResult));
        if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {

            Object data = responseResult.getData();
            ModfiyOrderResp modfiyOrderResp = JSONObject.from(data).to(ModfiyOrderResp.class);
            vo.setOrderId(modfiyOrderResp.getClientOid());
            log.info("订单修改成功，新订单号: {}",modfiyOrderResp.getClientOid());
        }
        return vo;
    }

    @Override
    public void preCheckOrder(OrderVo order) {
        CoinContractInfomation contractCoinInfo = this.getContractCoinInfo(order.getAccount(), order.getSymbol());

        if(OrderType.LIMIT.equalsIgnoreCase(order.getOrderType())) {
            /**
             * bitget 每笔订单必须 下单数量要大于 minTradeNum 并且满足 sizeMulti 的倍数
             * 并且每笔价格大于 minTradeUSDT
             */
            BigDecimal tradeUsdt = order.getPrice().multiply(order.getSize());
            if (tradeUsdt.doubleValue() < contractCoinInfo.getMinTradeUSDT().doubleValue()) {
                throw new RuntimeException("下单价值低于 " + contractCoinInfo.getMinTradeUSDT() + " 下单价值：" + tradeUsdt);
            }
            /**
             * 修复价格步长
             */
            BigDecimal price = order.getPrice();
            BigDecimal priceAdjust = BigDecimalUtils.adjustPrice(price, contractCoinInfo.getPricePlace().intValue(), contractCoinInfo.getPriceEndStep().intValue());
            order.setPrice(priceAdjust);
        }


        if (order.getSize().doubleValue() < contractCoinInfo.getMinSz().doubleValue()) {
            throw new RuntimeException("下单数量不满足最低下单数量 " + contractCoinInfo.getMinSz() + " 下单数量：" + order.getSize());
        }
        if (!BigDecimalUtils.isMultiple(order.getSize(), contractCoinInfo.getSizeMultiplier())) {

            throw new RuntimeException("下单数量要大于 minTradeNum 并且满足 sizeMulti 的倍数 sizeMylti：" + contractCoinInfo.getSizeMultiplier() + " 下单数量：" + order.getSize());
        }


    }

    @SneakyThrows
    @Override
    public List<LinerSymbol> getAllLinerSymbol() {
        ResponseResult responseResult = noAuthEndpoint.request().get("/api/v2/mix/market/contracts", Map.of("productType", "USDT-FUTURES"));
        List<LinerSymbol> linerSymbols = new ArrayList<>();
        if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {
            Object data = responseResult.getData();
            List<UmcblItemV2> list = JSONArray.from(data).toList(UmcblItemV2.class);
            for (UmcblItemV2 umcblItemV2 : list) {
                LinerSymbol linerSymbol = new LinerSymbol();
                linerSymbol.setSymbol(umcblItemV2.getSymbol());
                linerSymbol.setCoin(umcblItemV2.getBaseCoin());
                linerSymbol.setFundingInterval(umcblItemV2.getFundInterval());
                linerSymbols.add(linerSymbol);
            }
            return linerSymbols;
        }
        return List.of();
    }

    @SneakyThrows
    @Override
    public Integer getLinerSymbolFundingRateInterval(String symbol) {
        ResponseResult responseResult = noAuthEndpoint.request().get("/api/v2/mix/market/contracts", Map.of("productType", "USDT-FUTURES","symbol",checkSymbolLinerV2(symbol)));
        if (responseResult.getCode().equalsIgnoreCase(SUCCESS)) {
            Object data = responseResult.getData();
            List<UmcblItemV2> list = JSONArray.from(data).toList(UmcblItemV2.class);
            for (UmcblItemV2 umcblItemV2 : list) {
                if(umcblItemV2.getSymbol().equals(checkSymbolLinerV2(symbol))){
                    return umcblItemV2.getFundInterval();
                }
            }
        }
        return 0;
    }

    @Override
    public ContractOrder formateOrderBySyncOrderInfo(OrderOptStatus orderStatus,Account account,SyncOrderDetail syncOrderDetail) {
        return null;
    }

    @SneakyThrows
    @Override
    public List<PositionWsData> queryContractPositionDetail(Account account, PositionParams params) {
        String paptrading = getPaptrading();
        BitgetRestClient client = BitgetClientService.createClient(account.getApiKey(), account.getApiSecret(), account.getPassphrase(), null,paptrading);
        List<PositionWsData> wsDatas = new ArrayList<>();

        ResponseResult responseResult = client.bitget().v2().mixAccount().allPosition(Map.of("productType", "USDT-FUTURES"),"1".equalsIgnoreCase(paptrading) ? Map.of("paptrading","1"):Map.of());
        if (SUCCESS.equalsIgnoreCase(responseResult.getCode())) {
            Object data = responseResult.getData();
            List<BitgetPositionDetail> positionDetails = JSONArray.from(data).toList(BitgetPositionDetail.class);
            for (BitgetPositionDetail positionDetail : positionDetails) {
                PositionWsData positionWsData = new PositionWsData();
                positionWsData.setSymbol(deSymbolLinerV2(positionDetail.getSymbol()));
                positionWsData.setAccountName(account.getName());
                positionWsData.setAccountId(account.getId());
                positionWsData.setExchange(ExchangeType.BITGET.getName());

                positionWsData.setLiqPrice(positionDetail.getLiquidationPrice());

                if (positionDetail.getHoldSide().equalsIgnoreCase(SideType.SHORT)) {
                    positionWsData.setSide(SideType.SHORT);
                }else if(positionDetail.getHoldSide().equalsIgnoreCase(SideType.LONG)){
                    positionWsData.setSide(SideType.LONG);
                }
                positionWsData.setPosType(PositionType.SWAP);
                positionWsData.setFee(positionDetail.getDeductedFee());
                positionWsData.setSize(positionDetail.getTotal());
                positionWsData.setFundingFee(positionDetail.getTotalFee());
                positionWsData.setProfit(positionDetail.getAchievedProfits());
                positionWsData.setAchievedProfits(positionDetail.getAchievedProfits());
                positionWsData.setTotalFee(positionDetail.getTotalFee());
                positionWsData.setUnrealizedPL(positionDetail.getUnrealizedPL());
                positionWsData.setKeepMarginRate(positionDetail.getKeepMarginRate());
                positionWsData.setUnrealizedProfit(positionDetail.getUnrealizedPL());
                positionWsData.setMarginPrice(positionDetail.getMarginSize());
                positionWsData.setMarginRatio(positionDetail.getKeepMarginRate());
                positionWsData.setLeverage(positionDetail.getLeverage());
                positionWsData.setMarginType(positionDetail.getMarginMode());
                positionWsData.setAvgPrice(positionDetail.getOpenPriceAvg());
                positionWsData.setUpdateTime(new Date(positionDetail.getUTime()));
                positionWsData.setCreateTime(new Date(positionDetail.getCTime()));
                positionWsData.setAccountName(account.getName());
                positionWsData.setServerTime(new Date());
                positionWsData.setTakeProfit(positionDetail.getTakeProfit());
                positionWsData.setStopLoss(positionDetail.getStopLoss());
                positionWsData.setHoldSide(positionDetail.getHoldSide());
                wsDatas.add(positionWsData);
            }
        }else{
            log.error("查询合约持仓详情失败：{}", responseResult.getMsg());
        }

        return wsDatas;
    }

    @SneakyThrows
    @Override
    public List< ? extends TpSlOrder> queryContractTpSlOrder(Account account, String symbol) {
        String paptrading = getPaptrading();
        BitgetRestClient client = BitgetClientService.createClient(account.getApiKey(), account.getApiSecret(), account.getPassphrase(), null,paptrading);
        /**
         * https://www.bitget.com/zh-CN/api-doc/contract/plan/get-orders-plan-pending
         */
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("symbol", checkSymbolLinerV2(symbol));
        queryParams.put("productType", "USDT-FUTURES");
        queryParams.put("planType","profit_loss");
        ResponseResult responseResult = client.bitget().v2().mixOrder().ordersPlanPending(queryParams);
        if (SUCCESS.equalsIgnoreCase(responseResult.getCode())) {
            Object data = responseResult.getData();
            JSONObject from = JSONObject.from(data);
            JSONArray entrustedList = from.getJSONArray("entrustedList");
            if (Objects.isNull(entrustedList)) {
                return List.of();
            }
            List<EntrustedData> javaList = entrustedList.toJavaList(EntrustedData.class);
            for (EntrustedData entrustedData : javaList) {
                entrustedData.setSymbol(symbol);
                entrustedData.setTakeProfitPrice(entrustedData.getStopSurplusTriggerPrice());
                entrustedData.setStopLossPrice(entrustedData.getStopLossTriggerPrice());
            }
            return javaList;
        }

        return new ArrayList<>();
    }

    @SneakyThrows
    @Override
    public void updateContractTpSl(Account account, String symbol, TpSlOrder tpSlOrder) {
        if(tpSlOrder instanceof UpdateTpSl updateTpSl){
            String paptrading = getPaptrading();
            BitgetRestClient client = BitgetClientService.createClient(account.getApiKey(), account.getApiSecret(), account.getPassphrase(), null,paptrading);
            /**
             * https://www.bitget.com/zh-CN/api-doc/contract/plan/get-orders-plan-pending
             */
            HashMap<String, String> queryParams = new HashMap<>();
            queryParams.put("symbol", checkSymbolLinerV2(updateTpSl.getSymbol()));
            queryParams.put("productType", "USDT-FUTURES");
            queryParams.put("orderId",updateTpSl.getOrderId());
            queryParams.put("triggerPrice",updateTpSl.getTriggerPrice());
            queryParams.put("triggerType",updateTpSl.getTriggerType());
            queryParams.put("executePrice", updateTpSl.getExecutePrice());
            queryParams.put("marginCoin", "USDT");
            queryParams.put("size", "");
            ResponseResult responseResult = client.bitget().v2().mixOrder().modifyTpslOrder(queryParams);
            if (SUCCESS.equalsIgnoreCase(responseResult.getCode())) {
                log.info("修改止盈止损成功 {}",JSONObject.toJSONString(responseResult));
            }else{
                log.error("修改止盈止损失败 {}",JSONObject.toJSONString(responseResult));
            }
        }

    }

    @SneakyThrows
    @Override
    public void createTpSl(Account account, String symbol, CreateTpSlOnce createTpSlOnce) {
        String paptrading = getPaptrading();
        BitgetRestClient client = BitgetClientService.createClient(account.getApiKey(), account.getApiSecret(), account.getPassphrase(), null,paptrading);
        /**
         * https://www.bitget.com/zh-CN/api-doc/contract/plan/get-orders-plan-pending
         */
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("symbol", checkSymbolLinerV2(createTpSlOnce.getSymbol()));
        queryParams.put("productType", "USDT-FUTURES");
        queryParams.put("marginCoin", "USDT");
        queryParams.put("stopSurplusTriggerType", createTpSlOnce.getStopSurplusTriggerType());
        queryParams.put("stopSurplusTriggerPrice", createTpSlOnce.getStopSurplusTriggerPrice());
        queryParams.put("stopLossTriggerPrice", createTpSlOnce.getStopLossTriggerPrice());
        queryParams.put("stopLossTriggerType", createTpSlOnce.getStopLossTriggerType());
        queryParams.put("holdSide", createTpSlOnce.getHoldSide());


        ResponseResult responseResult = client.bitget().v2().mixOrder().placePosTpslOrder(queryParams);
        if (SUCCESS.equalsIgnoreCase(responseResult.getCode())) {
            log.info("创建止盈止损成功 {}",JSONObject.toJSONString(responseResult));
        }else{
            log.error("创建止盈止损失败 {}",JSONObject.toJSONString(responseResult));
        }

    }

    /**
     * support k线粒度
     * - 1m(1分钟)
     * - 3m(3分钟)
     * - 5m(5分钟)
     * - 15m(15分钟)
     * - 30m(30分钟)
     * - 1H(1小时)
     * - 4H(4小时)
     * - 6H(6小时)
     * - 12H(12小时)
     * - 1D(1天)
     * - 3D (3天)
     * - 1W(1周)
     * - 1M (月线)
     *
     * okx
     * 时间粒度，默认值1m
     * 如 [1m/3m/5m/15m/30m/1H/2H/4H] 6H/12H/1D/2D/3D/1W/1M/3M
     *
     * 共性
     * 1m/3m/5m/15m/30m/1H/4H/6H/12H/1D/3D/1W/1M
     * @param account
     * @param symbol
     * @param granularity
     * @param limit
     * @return
     */
    @SneakyThrows
    @Override
    public JSONArray getMarketCandles(Account account, String symbol,String granularity,Long limit) {
        BitgetRestClient client = getClient(account, null);

        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("productType", "USDT-FUTURES");
        queryParams.put("symbol", checkSymbolLinerV2(symbol));
        queryParams.put("granularity", granularity);
        queryParams.put("limit",Objects.isNull(limit)?"50":limit+"");
        ResponseResult responseResult = client.bitget().v2().mixMarket().candles(queryParams);
        if (SUCCESS.equalsIgnoreCase(responseResult.getCode())) {
            Object data = responseResult.getData();
            return JSONArray.from(data);
        }else{
            log.error("创建止盈止损失败 {}",JSONObject.toJSONString(responseResult));
        }

        return new JSONArray();
    }

    @SneakyThrows
    @Override
    public BigDecimal getOpenInterest(Account account, String symbol) {
        BitgetRestClient client = getClient(account, null);

        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("productType", "USDT-FUTURES");
        queryParams.put("symbol", checkSymbolLinerV2(symbol));
        ResponseResult responseResult = client.bitget().v2().mixMarket().openInterest(queryParams);
        if (SUCCESS.equalsIgnoreCase(responseResult.getCode())) {
            Object data = responseResult.getData();
            JSONObject from = JSONObject.from(data);
            JSONArray openInterestList = from.getJSONArray("openInterestList");
            if (CollectionUtils.isEmpty(openInterestList)) {
                return BigDecimal.ZERO;
            }
            JSONObject item = openInterestList.getJSONObject(0);

            return item.getBigDecimal("size");
        }else{
            log.error("获取交易量失败 {}",JSONObject.toJSONString(responseResult));
        }
        throw new RuntimeException("error");
    }

    @SneakyThrows
    @Override
    public List<HistoryPosition> queryContractHistoryPosition(Account account, Long size,HistoryPositionQuery query) {

        BitgetRestClient client = getClient(account, null);

        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("productType", "USDT-FUTURES");
        queryParams.put("limit", size+"");
        if (Objects.nonNull(query)) {
            if(Objects.nonNull(query.getIdLessThan())){
                queryParams.put("idLessThan", query.getIdLessThan());
            }

            if(Objects.nonNull(query.getStartTime())){
                queryParams.put("startTime", query.getStartTime().getTime()+"");
            }
            if(Objects.nonNull(query.getEndTime())){
                queryParams.put("startTime", query.getEndTime().getTime()+"");
            }
            if(StringUtils.isNotEmpty(query.getSymbol())){
                String symbol = checkSymbolLinerV2(query.getSymbol());
                queryParams.put("symbol", symbol);
            }
        }


        ResponseResult responseResult = client.bitget().v2().mixAccount().historyPosition(queryParams);
        if (SUCCESS.equalsIgnoreCase(responseResult.getCode())) {
            Object data = responseResult.getData();
            JSONObject from = JSONObject.from(data);
            JSONArray openInterestList = from.getJSONArray("list");
            if (CollectionUtils.isEmpty(openInterestList)) {
                return new ArrayList<>();
            }
            List<HistoryPosition> javaList = openInterestList.toJavaList(HistoryPosition.class);
            for (HistoryPosition historyPosition : javaList) {
                historyPosition.setIdLessThan(from.getString("endId"));
            }

            return javaList;
        }else{
            log.error("获取交易量失败 {}",JSONObject.toJSONString(responseResult));
        }
        throw new RuntimeException("error");

    }

    @Override
    public String getPositionId(HistoryPosition historyPosition) {
        String ctime = historyPosition.getCtime();
        String symbol = historyPosition.getSymbol();
        String utime = historyPosition.getUtime();
        return ctime + utime + symbol;
    }
}
