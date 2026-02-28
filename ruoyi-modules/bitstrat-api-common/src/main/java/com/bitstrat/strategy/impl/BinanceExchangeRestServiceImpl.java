package com.bitstrat.strategy.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.bitstrat.annotation.AccountPaptrading;
import com.bitstrat.constant.*;
import com.bitstrat.constant.binance.BinanceOrderType;
import com.bitstrat.constant.binance.BinanceUMFilterType;
import com.bitstrat.domain.*;
import com.bitstrat.domain.binance.*;
import com.bitstrat.domain.bitget.TickerItem;
import com.bitstrat.domain.vo.SymbolFundingRate;
import com.bitstrat.store.OrderStatusConstant;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.BigDecimalUtils;
import com.bitstrat.utils.StringListUtil;
import com.bitstrat.wsClients.msg.BinanceWsMsg;
import com.bitstrat.wsClients.msg.SubscriptMsgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 币安Restapi
 */
@Slf4j
@Service
@AccountPaptrading
public class BinanceExchangeRestServiceImpl implements ExchangeService {
    BinanceWsMsg binanceWsMsg = new BinanceWsMsg();

    @Override
    public SubscriptMsgs getWsSubscriptMsgs() {

        return binanceWsMsg;
    }

    @Override
    public String getExchangeName() {
        return ExchangeType.BINANCE.getName();
    }

    @Override
    public CoinContractInfomation getContractCoinInfo(Account account, String symbol) {
        BinanceExchangeRestServiceImpl bean = SpringUtils.getBean(this.getClass());
        List<UMSymbolInfoDetail> umFuturesClients = bean.queryLinerInfo();
        Map<String, UMSymbolInfoDetail> symbolInfoDetailMap = umFuturesClients.stream()
            .collect(Collectors.toMap(UMSymbolInfoDetail::getSymbol, umSymbolInfoDetail -> umSymbolInfoDetail
                , (a, b) -> a));
        if (symbolInfoDetailMap.containsKey(checkSymbolLiner(symbol))) {
            UMSymbolInfoDetail umSymbolInfoDetail = symbolInfoDetailMap.get(checkSymbolLiner(symbol));
            CoinContractInfomation coinContractInfomation = new CoinContractInfomation();
            coinContractInfomation.setSymbol(symbol);
            coinContractInfomation.setPricePlace(umSymbolInfoDetail.getPricePrecision());
            coinContractInfomation.setCalcPlaces(umSymbolInfoDetail.getQuantityPrecision());
            List<Filter> filters = umSymbolInfoDetail.getFilters();
            coinContractInfomation.setFilters(filters);
            for (Filter filter : filters) {
                if (filter.getFilterType().equalsIgnoreCase(BinanceUMFilterType.PRICE_FILTER)) {
                    //价格限制
//                    coinContractInfomation.setMinSz(filter.getMinQty());

                }
                if (filter.getFilterType().equalsIgnoreCase(BinanceUMFilterType.LOT_SIZE)) {
                    //数量限制
                    coinContractInfomation.setMinSz(filter.getMinQty());
                    coinContractInfomation.setStep(filter.getStepSize());
                    coinContractInfomation.setMaxLmtSz(filter.getMaxQty());
                    coinContractInfomation.setMaxMktSz(filter.getMaxQty());

                }
                if (filter.getFilterType().equalsIgnoreCase(BinanceUMFilterType.MARKET_LOT_SIZE)) {
                    //市价单数量限制
                    coinContractInfomation.setMaxLmtSz(filter.getMaxQty());

                }
                if (filter.getFilterType().equalsIgnoreCase(BinanceUMFilterType.MIN_NOTIONAL)) {
                    //最小名义价值
                    coinContractInfomation.setMinTradeUSDT(filter.getNotional());

                }
                if (filter.getFilterType().equalsIgnoreCase(BinanceUMFilterType.PERCENT_PRICE)) {
                    //价格比限制
                    coinContractInfomation.setMultiplierUp(filter.getMultiplierUp());
                    coinContractInfomation.setMultiplierDown(filter.getMultiplierDown());

                }


            }

            LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("symbol", checkSymbolLiner(symbol));
            UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());
            String leverageBracket = client.account().getLeverageBracket(parameters);
            List<LeverageBracketItem> list = JSONArray.parseArray(leverageBracket).toList(LeverageBracketItem.class);
            if (CollectionUtils.isNotEmpty(list)) {
                LeverageBracketItem leverageBracketItem = list.get(0);
                //币安的杠杆分了很多层，第一层是杠杆最大的，同时也是次杠杆下持仓量最小的
                for (Bracket bracket : leverageBracketItem.getBrackets()) {
                    if (Objects.nonNull(bracket)) {
                        coinContractInfomation.setMaxLeverage(bracket.getInitialLeverage());
                        coinContractInfomation.setMinLeverage(1);
                        return coinContractInfomation;
                    }
                }
            }

            return coinContractInfomation;

        }


        return null;
    }

    @Override
    public String setLeverage(Account account, Integer leverage, String symbol, String side) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", checkSymbolLiner(symbol));
        parameters.put("leverage", leverage);
        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());
        String s = client.account().changeInitialLeverage(parameters);
        /**
         * {
         *  	"leverage": 21,	// 杠杆倍数
         *  	"maxNotionalValue": "1000000", // 当前杠杆倍数下允许的最大名义价值
         *  	"symbol": "BTCUSDT"	// 交易对
         * }
         */

        ChangeLeverageResult changeLeverageResult = JSONObject.parseObject(s).to(ChangeLeverageResult.class);

        if (Objects.equals(changeLeverageResult.getLeverage(), leverage)) {
            return CorssLeverageStatus.SUCCESS;
        }

        //合约默认修改为全仓模式
        LinkedHashMap<String, Object> changeMarginTypeParams = new LinkedHashMap<>();
        changeMarginTypeParams.put("symbol", checkSymbolLiner(symbol));

        //保证金模式 ISOLATED(逐仓), CROSSED(全仓)
        changeMarginTypeParams.put("marginType", "CROSSED");
        String changed = client.account().changeMarginType(changeMarginTypeParams);
        log.info("change biance margin Type: " + changed);

        throw new RuntimeException("change leverage failed " + s);
    }

    /**
     * 查询用户目前在 所有symbol 合约上的持仓模式：双向持仓或单向持仓。
     *
     * @param account
     * @return "true": 双向持仓模式；"false": 单向持仓模式
     */
    public Boolean queryPositionSide(Account account) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());
        String currentPositionMode = client.account().getCurrentPositionMode(parameters);
        /**
         * {
         * 	"dualSidePosition": true // "true": 双向持仓模式；"false": 单向持仓模式
         * }
         */
        JSONObject parsed = JSONObject.parseObject(currentPositionMode);
        if (parsed.containsKey("dualSidePosition")) {
            return parsed.getBoolean("dualSidePosition");
        }
        throw new RuntimeException("query currentPositionMode failed " + currentPositionMode);
    }

    @Override
    public boolean checkApi(Account account) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());

        try {
            String result = client.account().futuresAccountBalance(parameters);
            return true;
        } catch (BinanceConnectorException e) {
            log.error("fullErrMessage: {}", e.getMessage(), e);
        } catch (BinanceClientException e) {
            log.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
        }


        return false;
    }

    /**
     * U本位合约下单
     * https://developers.binance.com/docs/zh-CN/derivatives/usds-margined-futures/trade/rest-api
     *
     * @param account
     * @param params
     * @return
     */
    @Override
    public OrderOptStatus buyContract(Account account, OrderVo params) {
        //"true": 双向持仓模式；"false": 单向持仓模式
        Boolean positionSide = this.queryPositionSide(account);

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", checkSymbolLiner(params.getSymbol()));
        /**
         * 买卖方向 SELL, BUY
         */
        parameters.put("side", "BUY");
        /**
         * 持仓方向，单向持仓模式下非必填，默认且仅可填BOTH;在双向持仓模式下必填,且仅可选择 LONG 或 SHORT
         */
        if (positionSide) {
            parameters.put("positionSide", "LONG");
        } else {
//            parameters.put("positionSide", "BOTH");
        }

        /**
         * 订单类型 LIMIT, MARKET, STOP, TAKE_PROFIT, STOP_MARKET, TAKE_PROFIT_MARKET, TRAILING_STOP_MARKET
         */

        if (OrderType.MARKET.equalsIgnoreCase(params.getOrderType())) {
            parameters.put("type", BinanceOrderType.MARKET);
        } else if (OrderType.LIMIT.equalsIgnoreCase(params.getOrderType())) {
            parameters.put("type", BinanceOrderType.LIMIT);
            /**
             * 委托价格
             */
            parameters.put("price", params.getPrice());
            parameters.put("timeInForce", "GTC");
        }

        //是否只减仓
        /**
         * true, false; 非双开模式下默认false；双开模式下不接受此参数； 使用closePosition不支持此参数。
         */
        if (Objects.nonNull(params.getReduceOnly()) && params.getReduceOnly()) {
//            parameters.put("reduceOnly", "true");
            //买入 就是平空
            /**
             * https://dev.binance.vision/t/closing-the-long-position-with-hedge-mode/1000
             * By default the futures keeps the position mode to One-way. In order to enable the new feature of Hedge Mode, so you can have dual sides positions. enable it by endpoint POST /fapi/v1/positionSide/dual, setting the parameter dualSidePosition = true Open position: Long : positionSide=LONG, side=BUY Short: positionSide=SHORT, side=SELL Close position: Close long position: positionSide=LONG, side=SELL Close short position: positionSide=SHORT, side=BUY
             */
            parameters.put("positionSide", "SHORT");
        }

        /**
         * 下单数量,使用closePosition不支持此参数。
         */
        parameters.put("quantity", params.getSize());


        /**
         * 用户自定义的订单号，不可以重复出现在挂单中。如空缺系统会自动赋值。必须满足正则规则 ^[\.A-Z\:/a-z0-9_-]{1,36}$
         */
        String orderId = IdUtil.getSnowflake().nextIdStr();
        parameters.put("newClientOrderId", orderId);



        /**
         * "ACK", "RESULT", 默认 "ACK"
         */
        parameters.put("newOrderRespType", "RESULT");


        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());
        String s = client.account().newOrder(parameters);
        log.info("binance 下单结果:{}", s);
        BinanceOrderDetail binanceOrderDetail = JSONObject.parseObject(s).to(BinanceOrderDetail.class);
        log.info("binance 下单自定义id:{} binance order id:{}", binanceOrderDetail.getClientOrderId(), binanceOrderDetail.getOrderId());

        OrderOptStatus orderOptStatus = new OrderOptStatus();
        orderOptStatus.setOrderId(binanceOrderDetail.getOrderId() + "");
        orderOptStatus.setSymbol(binanceOrderDetail.getSymbol());
        orderOptStatus.setStatus(CrossOrderStatus.SUCCESS);
        SyncOrderDetail syncOrderDetail = new SyncOrderDetail();
        syncOrderDetail.setBinanceOrderDetail(binanceOrderDetail);
        orderOptStatus.setSyncOrderDetail(syncOrderDetail);
        orderOptStatus.setSyncOrder(true);


        return orderOptStatus;
    }

    @Override
    public OrderOptStatus sellContract(Account account, OrderVo params) {
        //"true": 双向持仓模式；"false": 单向持仓模式
        Boolean positionSide = this.queryPositionSide(account);

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", checkSymbolLiner(params.getSymbol()));
        /**
         * 买卖方向 SELL, BUY
         */
        parameters.put("side", "SELL");
        /**
         * 持仓方向，单向持仓模式下非必填，默认且仅可填BOTH;在双向持仓模式下必填,且仅可选择 LONG 或 SHORT
         */
        if (positionSide) {
            parameters.put("positionSide", "SHORT");
        } else {
//            parameters.put("positionSide", "BOTH");
        }

        /**
         * 订单类型 LIMIT, MARKET, STOP, TAKE_PROFIT, STOP_MARKET, TAKE_PROFIT_MARKET, TRAILING_STOP_MARKET
         */

        if (OrderType.MARKET.equalsIgnoreCase(params.getOrderType())) {
            parameters.put("type", BinanceOrderType.MARKET);
        } else if (OrderType.LIMIT.equalsIgnoreCase(params.getOrderType())) {
            parameters.put("type", BinanceOrderType.LIMIT);
            /**
             * 委托价格
             */
            parameters.put("price", params.getPrice());
            parameters.put("timeInForce", "GTC");
        }

        //是否只减仓
        /**
         * true, false; 非双开模式下默认false；双开模式下不接受此参数； 使用closePosition不支持此参数。
         */
        if (Objects.nonNull(params.getReduceOnly()) && params.getReduceOnly()) {
//            parameters.put("reduceOnly", "true");
            //卖出 就是平多
            /**
             * https://dev.binance.vision/t/closing-the-long-position-with-hedge-mode/1000
             * By default the futures keeps the position mode to One-way. In order to enable the new feature of Hedge Mode, so you can have dual sides positions. enable it by endpoint POST /fapi/v1/positionSide/dual, setting the parameter dualSidePosition = true Open position: Long : positionSide=LONG, side=BUY Short: positionSide=SHORT, side=SELL Close position: Close long position: positionSide=LONG, side=SELL Close short position: positionSide=SHORT, side=BUY
             */
            parameters.put("positionSide", "LONG");
        }

        /**
         * 下单数量,使用closePosition不支持此参数。
         */
        parameters.put("quantity", params.getSize());


        /**
         * 用户自定义的订单号，不可以重复出现在挂单中。如空缺系统会自动赋值。必须满足正则规则 ^[\.A-Z\:/a-z0-9_-]{1,36}$
         */
        String orderId = IdUtil.getSnowflake().nextIdStr();
        parameters.put("newClientOrderId", orderId);



        /**
         * "ACK", "RESULT", 默认 "ACK"
         */
        parameters.put("newOrderRespType", "RESULT");


        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());
        String s = client.account().newOrder(parameters);
        log.info("binance 下单结果:{}", s);
        BinanceOrderDetail binanceOrderDetail = JSONObject.parseObject(s).to(BinanceOrderDetail.class);
        log.info("binance 下单自定义id:{} binance order id:{}", binanceOrderDetail.getClientOrderId(), binanceOrderDetail.getOrderId());

        OrderOptStatus orderOptStatus = new OrderOptStatus();
        orderOptStatus.setOrderId(binanceOrderDetail.getOrderId() + "");
        orderOptStatus.setSymbol(binanceOrderDetail.getSymbol());
        orderOptStatus.setStatus(CrossOrderStatus.SUCCESS);
        SyncOrderDetail syncOrderDetail = new SyncOrderDetail();
        syncOrderDetail.setBinanceOrderDetail(binanceOrderDetail);
        orderOptStatus.setSyncOrderDetail(syncOrderDetail);
        orderOptStatus.setSyncOrder(true);


        return orderOptStatus;
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

    @Override
    public String cancelContractOrder(Account account, OrderOptStatus order) {
        String symbolLiner = checkSymbolLiner(order.getSymbol());
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();

        params.put("symbol", symbolLiner);
        params.put("orderId", order.getOrderId());
        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());
        try {
            String result = client.account().cancelOrder(params);
            BinanceOrderDetail binanceOrderDetail = JSONObject.parseObject(result).to(BinanceOrderDetail.class);
            log.info("币安 binance 取消订单响应结果 ：{}", result);
            if (binanceOrderDetail.getStatus().equalsIgnoreCase("CANCELED")) {
                return CrossOrderStatus.SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.error("取消订单失败！:{}", order.getOrderId());
        return CrossOrderStatus.FAIL;
    }

    @Override
    public OrderCloseResult closeContractPosition(Account account, OrderPosition order) {
        return new OrderCloseResult();
    }

    @Override
    public OrderPosition queryContractPosition(Account account, String symbol, PositionParams params) {
        //先查持仓模式 "true": 双向持仓模式；"false": 单向持仓模式
        Boolean positionSide = queryPositionSide(account);

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());
        parameters.put("symbol", checkSymbolLiner(symbol));
        String s = client.account().positionInformation(parameters);
        log.info("币安查询持仓信息 symbol:{} 结果:{}", symbol, s);
        List<UMPositionItem> list = JSONArray.parseArray(s).toList(UMPositionItem.class);
        if (positionSide) {
            //双向持仓只拿有持仓数量的处理
            for (UMPositionItem position : list) {
                if (position.getSymbol().equalsIgnoreCase(checkSymbolLiner(symbol)) &&
                    position.getPositionAmt().abs().compareTo(BigDecimal.ZERO) > 0) {
                    //处理仓位
                    OrderPosition orderPosition = new OrderPosition();
                    String side = getPositionSideDoubleSide(position);
                    orderPosition.setSize(position.getPositionAmt().abs());
                    if (SideType.EMPTY.equalsIgnoreCase(side)) {
                        log.info("仓位已平 ｛｝", orderPosition.getPositionId());
                    }
//                    orderPosition.setFee();
                    orderPosition.setLever(position.getLeverage());
                    orderPosition.setEx(ExchangeType.BINANCE.getName());
                    orderPosition.setSymbol(symbol);
                    //币安查询资金费接口 https://developers.binance.com/docs/zh-CN/derivatives/portfolio-margin/account/Get-UM-Income-History
//                    orderPosition.setFundingFee(position.getBigDecimal("fundingFee"));
//                    orderPosition.setMgnRatio(position.getBigDecimal("mgnRatio"));
                    orderPosition.setBreakEvenPrice(position.getBreakEvenPrice());
                    orderPosition.setRealizedPnl(position.getUnRealizedProfit());
                    orderPosition.setProfit(position.getUnRealizedProfit());
                    orderPosition.setSettledPnl(BigDecimal.ZERO);
                    orderPosition.setAvgPrice(position.getEntryPrice());
                    orderPosition.setLiqPx(position.getLiquidationPrice());

                    orderPosition.setSide(side);
                    orderPosition.setAccountId(account.getId());
                    return orderPosition;

                }
            }

        } else {
            //单向持仓
            for (UMPositionItem position : list) {
                if (position.getSymbol().equalsIgnoreCase(checkSymbolLiner(symbol))) {
                    //处理仓位
                    OrderPosition orderPosition = new OrderPosition();
                    String side = getPositionSide(position);
                    orderPosition.setSize(position.getPositionAmt().abs());
                    if (SideType.EMPTY.equalsIgnoreCase(side)) {
                        log.info("仓位已平 ｛｝", orderPosition.getPositionId());
                    }
//                    orderPosition.setFee();
                    orderPosition.setLever(position.getLeverage());
                    orderPosition.setEx(ExchangeType.BINANCE.getName());
                    orderPosition.setSymbol(symbol);
                    //币安查询资金费接口 https://developers.binance.com/docs/zh-CN/derivatives/portfolio-margin/account/Get-UM-Income-History
//                    orderPosition.setFundingFee(position.getBigDecimal("fundingFee"));
//                    orderPosition.setMgnRatio(position.getBigDecimal("mgnRatio"));
                    orderPosition.setBreakEvenPrice(position.getBreakEvenPrice());
                    orderPosition.setRealizedPnl(position.getUnRealizedProfit());
                    orderPosition.setProfit(position.getUnRealizedProfit());
                    orderPosition.setSettledPnl(BigDecimal.ZERO);
                    orderPosition.setAvgPrice(position.getEntryPrice());
                    orderPosition.setLiqPx(position.getLiquidationPrice());

                    orderPosition.setSide(side);
                    orderPosition.setAccountId(account.getId());
                    return orderPosition;

                }
            }

        }


        return null;
    }


    private String getPositionSide(UMPositionItem position) {
        int compared = position.getPositionAmt().compareTo(BigDecimal.ZERO);
        String side = null;
        if (compared > 0) {
            side = SideType.LONG;
        }
        if (compared < 0) {
            side = SideType.SHORT;
        }

        if (compared == 0) {
            //已平仓
            side = SideType.EMPTY;
        }
        return side;
    }

    private String getPositionSideDoubleSide(UMPositionItem position) {
        String side = null;
        if (position.getPositionSide().equalsIgnoreCase(SideType.LONG)) {
            side = SideType.LONG;
        }
        if (position.getPositionSide().equalsIgnoreCase(SideType.SHORT)) {
            side = SideType.SHORT;
        }

        return side;
    }

    @Override
    public BigDecimal queryClosePositionProfit(Account account, String symbol, PositionParams params) {
        String symbolLiner = checkSymbolLiner(symbol);
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());
        parameters.put("symbol", symbolLiner);
        parameters.put("startTime", params.getStartTime());
        parameters.put("limit", 500);
        String incomeHistory = client.account().getIncomeHistory(parameters);
        List<IncomeItem> incomeItems = JSONArray.parseArray(incomeHistory).toList(IncomeItem.class);
        BigDecimal profit = BigDecimal.ZERO;
        for (IncomeItem incomeItem : incomeItems) {
            if (incomeItem.getSymbol().equalsIgnoreCase(symbolLiner)) {
                profit = profit.add(incomeItem.getIncome());
            }
        }

        return profit;
    }

    @Override
    public SymbolFundingRate getSymbolFundingRate(String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        UMFuturesClientImpl client = new UMFuturesClientImpl();
        parameters.put("symbol", checkSymbolLiner(symbol));
        String s = client.market().markPrice(parameters);
        MarketInfo marketInfo = JSONObject.parseObject(s).to(MarketInfo.class);
        if (Objects.nonNull(marketInfo)) {
            SymbolFundingRate symbolFundingRate = new SymbolFundingRate();
            symbolFundingRate.setFundingRate(marketInfo.getLastFundingRate());
            symbolFundingRate.setNextFundingTime(marketInfo.getNextFundingTime());
            return symbolFundingRate;
        }

        return null;
    }

    @Cacheable(value = "binanceContractCoinInfo:cache#60s#60s#2000", key = "'binance'+':'+'ContractCoinInfo'+':SymbolFundingRate'")
    public List<FundingInfoItem> querySymbolFundingRate() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        UMFuturesClientImpl client = new UMFuturesClientImpl();
        String s = client.market().fundingInfo(parameters);
        List<FundingInfoItem> fundingInfoItems = JSONArray.parseArray(s, FundingInfoItem.class);
        if (CollectionUtils.isEmpty(fundingInfoItems)) {
            return new ArrayList<>();
        } else {
            return fundingInfoItems;
        }
    }

    /**
     * 获取所有合约详细信息
     *
     * @return
     */
    @Cacheable(value = "binanceContractCoinInfo:cache#60s#600s#2000", key = "'binance'+':'+'ContractCoinInfo'+':SymbolInfo'")
    public List<UMSymbolInfoDetail> queryLinerInfo() {
        UMFuturesClientImpl client = new UMFuturesClientImpl();
        String s = client.market().exchangeInfo();
        JSONObject jsonObject = JSONObject.parseObject(s);
        List<UMSymbolInfoDetail> symbols = jsonObject.getJSONArray("symbols").toList(UMSymbolInfoDetail.class);
        if (CollectionUtils.isEmpty(symbols)) {
            return new ArrayList<>();
        } else {
            return symbols;
        }
    }

    @Override
    public AccountBalance getBalance(Account account, String coin) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());

        try {
            String result = client.account().futuresAccountBalance(parameters);
            AccountBalance newBalance = new AccountBalance();
            List<BinanceBalanceItem> binanceBalanceItems = JSONArray.parseArray(result, BinanceBalanceItem.class);
            for (BinanceBalanceItem binanceBalanceItem : binanceBalanceItems) {
                if (binanceBalanceItem.getAsset().equals(coin)) {
                    newBalance.setBalance(binanceBalanceItem.getBalance());
                    newBalance.setEquity(binanceBalanceItem.getCrossWalletBalance().subtract(binanceBalanceItem.getCrossUnPnl()));
                    newBalance.setFreeBalance(binanceBalanceItem.getAvailableBalance());

                    newBalance.setApiId(account.getId());
                    newBalance.setApiName(account.getName());
                    return newBalance;
                }
            }
        } catch (BinanceConnectorException e) {
            log.error("fullErrMessage: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        } catch (BinanceClientException e) {
            log.error("fullErrMessage: {} \nerrMessage: {} \nerrCode: {} \nHTTPStatusCode: {}",
                e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode(), e);
            throw new RuntimeException(e.getMessage());
        }
        throw new RuntimeException("币安余额加载失败");
    }

    @Override
    public SymbolFee getFee(Account account, String coin) {


        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());
        parameters.put("symbol", checkSymbolLiner(coin));
        String result = client.account().getCommissionRate(parameters);
        CommissionRate commissionRate = JSONObject.parseObject(result).to(CommissionRate.class);

        SymbolFee symbolFee = new SymbolFee();
        symbolFee.setLinerTakerFeeRate(commissionRate.getTakerCommissionRate());
        symbolFee.setLinerMakerFeeRate(commissionRate.getMakerCommissionRate());

        return symbolFee;
    }

    @NotNull
    private static String checkSymbolLiner(String symbol) {
        if (!symbol.endsWith("USDT")) {
            symbol = symbol + "USDT";
        }
        if (symbol.endsWith("/USDT")) {
            symbol = symbol.replace("/", "");
        }
        return symbol.toUpperCase();
    }

    @Override
    public BigDecimal getNowPrice(Account account, String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        UMFuturesClientImpl client = new UMFuturesClientImpl();
        parameters.put("symbol", checkSymbolLiner(symbol));
        String s = client.market().markPrice(parameters);
        MarketInfo marketInfo = JSONObject.parseObject(s).to(MarketInfo.class);

        return marketInfo.getMarkPrice();
    }

    @Override
    public TickerItem getNowPrice(Account account, String symbol, String bitgetOnly) {
        return null;
    }

    @Override
    public OrderVo calcOrderSize(OrderVo longOrder) {
        //处理价格精度
        CoinContractInfomation contractCoinInfo = SpringUtils.getBean(this.getClass()).getContractCoinInfo(longOrder.getAccount(), longOrder.getSymbol());
        BigDecimal pricePlace = contractCoinInfo.getPricePlace();
//        BigDecimal formatePrice = longOrder.getPrice().setScale(pricePlace.intValue(), RoundingMode.DOWN);
        OrderVo orderVo = preCheckOrder(contractCoinInfo, longOrder);
        BigDecimal formatePrice = orderVo.getPrice();
        log.info("币安 下单价格：{} 预处理后价格:{}", longOrder.getPrice(), formatePrice);
        longOrder.setPrice(formatePrice);

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

    @Override
    public OrderOptStatus queryContractOrderStatus(Account account, OrderOptStatus order) {
        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", checkSymbolLiner(order.getSymbol()));
        params.put("orderId", Long.valueOf(order.getOrderId()));
        String queryOrder = client.account().queryOrder(params);
        log.info("币安 查询订单状态 oderId :{} response:{}", order.getOrderId(), queryOrder);
        BinanceQueryOrderDetail binanceQueryOrderDetail = JSONObject.parseObject(queryOrder).to(BinanceQueryOrderDetail.class);
        List<String> binanceProcessStatus = OrderStatusConstant.binanceProcessStatus;
        List<String> binanceEndStatus = OrderStatusConstant.binanceEndStatus;
        String status = binanceQueryOrderDetail.getStatus();
        if (StringListUtil.containsIgnoreCase(binanceProcessStatus, status)) {
            OrderOptStatus orderOptStatus = new OrderOptStatus();
            orderOptStatus.setSymbol(order.getSymbol());
            orderOptStatus.setOrderId(order.getOrderId());
            orderOptStatus.setSide(orderOptStatus.getSide());
            orderOptStatus.setStatus(CrossOrderStatus.PROCESS);
            return orderOptStatus;
        } else if (StringListUtil.containsIgnoreCase(binanceEndStatus, status)) {
            OrderOptStatus orderOptStatus = new OrderOptStatus();
            orderOptStatus.setSymbol(order.getSymbol());
            orderOptStatus.setOrderId(order.getOrderId());
            orderOptStatus.setSide(orderOptStatus.getSide());
            orderOptStatus.setStatus(CrossOrderStatus.END);
            return orderOptStatus;
        }

        OrderOptStatus orderOptStatus = new OrderOptStatus();
        orderOptStatus.setSymbol(order.getSymbol());
        orderOptStatus.setOrderId(order.getOrderId());
        orderOptStatus.setSide(orderOptStatus.getSide());
        orderOptStatus.setStatus(CrossOrderStatus.UNKNOW);
        return orderOptStatus;
    }

    @Override
    public List<ContractOrder> queryContractOrdersByIds(Account account, List<String> orderIds, String symbol) {
        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());

        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", checkSymbolLiner(symbol));
        Optional<Long> minOrderId = orderIds.stream().filter(NumberUtils::isParsable).map(Long::valueOf).min(Long::compareTo);

        HashSet<Long> findOrderSet = new HashSet<>();
        List<ContractOrder> contractOrders = new ArrayList<>();
        if (minOrderId.isEmpty()) {

        } else {
            //批量查询指定订单号之后的
            params.put("orderId", minOrderId.get());
            String allOrders = client.account().allOrders(params);
            log.info("币安批量查询订单 返回结果orderDetail: {}", allOrders);
            List<BinanceOrderDetail> list = JSONArray.parseArray(allOrders).toList(BinanceOrderDetail.class);
            Map<Long, BinanceOrderDetail> queryOrderMap = list.stream().collect(Collectors.toMap(BinanceOrderDetail::getOrderId, binanceOrderDetail -> binanceOrderDetail));
            for (String orderId : orderIds) {
                if (NumberUtils.isParsable(orderId)) {
                    Long orderIdLong = Long.valueOf(orderId);
                    if (queryOrderMap.containsKey(orderIdLong)) {
                        findOrderSet.add(orderIdLong);
                        BinanceOrderDetail binanceOrderDetail = queryOrderMap.get(orderIdLong);
                        OrderOptStatus orderOptStatus = new OrderOptStatus();
                        orderOptStatus.setSymbol(symbol);
                        SyncOrderDetail syncOrderDetail = new SyncOrderDetail();
                        syncOrderDetail.setBinanceOrderDetail(binanceOrderDetail);
                        ContractOrder contractOrder = this.formateOrderBySyncOrderInfo(orderOptStatus, account, syncOrderDetail);
                        contractOrders.add(contractOrder);
                    }
                }

            }

        }
        for (String orderId : orderIds) {
            if (NumberUtils.isParsable(orderId)) {
                Long orderIdLong = Long.valueOf(orderId);
                if (!findOrderSet.contains(orderIdLong)) {
                    //单独查订单号
                    LinkedHashMap<String, Object> singalOrderParam = new LinkedHashMap<>();
                    singalOrderParam.put("symbol", checkSymbolLiner(symbol));
                    singalOrderParam.put("orderId", orderIdLong);
                    String orderDetail = client.account().queryOrder(singalOrderParam);
                    log.info("币安查询订单{} 返回结果orderDetail: {}", orderIdLong, orderDetail);
                    BinanceOrderDetail binanceOrderDetail = JSONObject.parseObject(orderDetail).to(BinanceOrderDetail.class);
                    OrderOptStatus orderOptStatus = new OrderOptStatus();
                    orderOptStatus.setSymbol(symbol);
                    SyncOrderDetail syncOrderDetail = new SyncOrderDetail();
                    syncOrderDetail.setBinanceOrderDetail(binanceOrderDetail);
                    ContractOrder contractOrder = this.formateOrderBySyncOrderInfo(orderOptStatus, account, syncOrderDetail);
                    contractOrders.add(contractOrder);
                }
            }
        }


        return contractOrders;
    }


    @Override
    public OrderVo updateContractOrder(Account account, OrderVo vo) {
        if (vo.getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
            //市价单不允许修改
            throw new RuntimeException("币安市价单不允许修改订单");
        }
        vo = SpringUtils.getBean(this.getClass()).calcOrderSize(vo);

        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("symbol", checkSymbolLiner(vo.getSymbol()));
        params.put("orderId", Long.valueOf(vo.getOrderId()));
        if (vo.getSide().equalsIgnoreCase(SideType.LONG)) {
            //做多
            params.put("side", "BUY");
        } else if (vo.getSide().equalsIgnoreCase(SideType.SHORT)) {
            //做空
            params.put("side", "SELL");
        }
        params.put("quantity", vo.getOrderSize());
        params.put("price", vo.getPrice());


        String s = null;
        try {
            s = client.account().modifyOrder(params);
        } catch (BinanceClientException e) {
            if (e.getErrorCode() == -5027) {
                //No need to modify the order.
                log.warn("修改失败:{}", e.getErrMsg());
            }
        }
        log.info("币安 orderId :{}修改订单结果:{}", vo.getOrderId(), s);

        return vo;
    }

    @Override
    public void preCheckOrder(OrderVo orderVo) {
        CoinContractInfomation contractCoinInfo = SpringUtils.getBean(this.getClass()).getContractCoinInfo(orderVo.getAccount(), orderVo.getSymbol());
        orderVo = preCheckOrder(contractCoinInfo, orderVo);
    }

    public OrderVo preCheckOrder(CoinContractInfomation contractCoinInfo, OrderVo orderVo) {
        List<Filter> filters = contractCoinInfo.getFilters();
        for (Filter filter : filters) {
            if (filter.getFilterType().equalsIgnoreCase(BinanceUMFilterType.PRICE_FILTER)) {
                //价格限制
                if(OrderType.MARKET.equalsIgnoreCase(orderVo.getOrderType())) {
                    return orderVo;
                }
                BigDecimal bigDecimal = BigDecimalUtils.adjustBinancePrice(orderVo.getPrice(), filter.getTickSize().scale(), filter.getTickSize());
                orderVo.setPrice(bigDecimal);
                return orderVo;
            }
//            if (filter.getFilterType().equalsIgnoreCase(BinanceUMFilterType.LOT_SIZE)) {
//                //数量限制
//                coinContractInfomation.setMinSz(filter.getMinQty());
//                coinContractInfomation.setStep(filter.getStepSize());
//                coinContractInfomation.setMaxLmtSz(filter.getMaxQty());
//                coinContractInfomation.setMaxMktSz(filter.getMaxQty());
//
//            }
//            if (filter.getFilterType().equalsIgnoreCase(BinanceUMFilterType.MARKET_LOT_SIZE)
//            && orderVo.getOrderType().equalsIgnoreCase(OrderType.MARKET)) {
//                //市价单数量限制
//                coinContractInfomation.setMaxLmtSz(filter.getMaxQty());
//
//            }
//            if (filter.getFilterType().equalsIgnoreCase(BinanceUMFilterType.MIN_NOTIONAL)) {
//                //最小名义价值
//
//
//            }
//            if (filter.getFilterType().equalsIgnoreCase(BinanceUMFilterType.PERCENT_PRICE)) {
//                //价格比限制
//                coinContractInfomation.setMultiplierUp(filter.getMultiplierUp());
//                coinContractInfomation.setMultiplierDown(filter.getMultiplierDown());
//
//            }


        }
        return orderVo;
    }

    @Override
    public List<LinerSymbol> getAllLinerSymbol() {
        BinanceExchangeRestServiceImpl bean = SpringUtils.getBean(this.getClass());
        List<UMSymbolInfoDetail> umFuturesClients = bean.queryLinerInfo();
        List<LinerSymbol> linerSymbols = new ArrayList<>();
        for (UMSymbolInfoDetail umFuturesClient : umFuturesClients) {
            if (umFuturesClient.getStatus().equalsIgnoreCase("PENDING_TRADING")
                || umFuturesClient.getStatus().equalsIgnoreCase("CLOSE")) {

                log.info("binance 交易对状态异常 {} {}", umFuturesClient.getSymbol(), umFuturesClient.getStatus());
                continue;
            }
            LinerSymbol linerSymbol = new LinerSymbol();
            linerSymbol.setSymbol(umFuturesClient.getSymbol());
            linerSymbol.setCoin(umFuturesClient.getBaseAsset());
            linerSymbol.setFundingInterval(bean.getLinerSymbolFundingRateInterval(umFuturesClient.getBaseAsset()));
            linerSymbols.add(linerSymbol);
        }

        return linerSymbols;
    }

    @Override
    @Cacheable(value = "binanceLinerSymbolFundingRateInterval:cache#600s#700s#1000", key = "'binance'+':'+'LinerSymbolFundingRateInterval'+':'+#symbol", condition = "#symbol != null ")
    public Integer getLinerSymbolFundingRateInterval(String symbol) {
        List<FundingInfoItem> fundingInfoItems = SpringUtils.getBean(this.getClass()).querySymbolFundingRate();
        String checkEdSymbol = checkSymbolLiner(symbol);
        for (FundingInfoItem fundingInfoItem : fundingInfoItems) {
            if (fundingInfoItem.getSymbol().equalsIgnoreCase(checkEdSymbol)) {
                return Math.toIntExact(fundingInfoItem.getFundingIntervalHours());
            }
        }

        return 0;
    }


    @Override
    public ContractOrder formateOrderBySyncOrderInfo(OrderOptStatus orderStatus, Account account, SyncOrderDetail syncOrderDetail) {
        BinanceOrderDetail datumFrom = syncOrderDetail.getBinanceOrderDetail();
        if (Objects.isNull(datumFrom)) {
            return null;
        }
        ContractOrder contractOrder = new ContractOrder();

        List<String> binanceProcessStatus = OrderStatusConstant.binanceProcessStatus;
        List<String> binanceEndStatus = OrderStatusConstant.binanceEndStatus;
        //订单状态
        String state = datumFrom.getStatus();
        contractOrder.setEx(ExchangeType.BINANCE.getName());
        contractOrder.setOrderId(datumFrom.getOrderId() + "");
        contractOrder.setPrice(datumFrom.getPrice());
        contractOrder.setSize(datumFrom.getOrigQty());
        contractOrder.setStatus(datumFrom.getStatus().toLowerCase());
        //累计成交数量
        contractOrder.setCumExecQty(datumFrom.getExecutedQty());
        contractOrder.setAvgPrice(datumFrom.getAvgPrice());

        if (StringListUtil.containsIgnoreCase(binanceProcessStatus, state)) {
            //中间态
            contractOrder.setOrderEnd(false);
        } else if (StringListUtil.containsIgnoreCase(binanceEndStatus, state)) {
            //结束态
            LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("symbol", checkSymbolLiner(orderStatus.getSymbol()));
            parameters.put("orderId", datumFrom.getOrderId());
            UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());
            String s = client.account().accountTradeList(parameters);
            List<AccountTradeListItem> tradeListItems = JSONArray.parseArray(s).toList(AccountTradeListItem.class);
            Map<Long, List<AccountTradeListItem>> accountTradeMap = tradeListItems.stream().collect(Collectors.groupingBy(AccountTradeListItem::getOrderId));
            List<AccountTradeListItem> feeOrderList = accountTradeMap.getOrDefault(datumFrom.getOrderId(), new ArrayList<>());

            BigDecimal totalFee = feeOrderList.stream().map(AccountTradeListItem::getCommission).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalExecValue = feeOrderList.stream().map(AccountTradeListItem::getQuoteQty).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalQty = feeOrderList.stream().map(AccountTradeListItem::getQty).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalPnl = feeOrderList.stream().map(AccountTradeListItem::getRealizedPnl).reduce(BigDecimal.ZERO, BigDecimal::add);
            //处理fee
            contractOrder.setFee(totalFee.negate());
            contractOrder.setCumExecValue(totalExecValue);
            contractOrder.setCumExecQty(totalQty);
            contractOrder.setPnl(totalPnl);

            contractOrder.setOrderEnd(true);

        }

        return contractOrder;
    }

    /**
     * 查询详细持仓信息
     *
     * @param account
     * @param params
     * @return
     */
    public List<PositionWsData> queryContractPositionDetail(Account account, PositionParams params) {
        //先查持仓模式 "true": 双向持仓模式；"false": 单向持仓模式
        Boolean positionSide = queryPositionSide(account);

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        UMFuturesClientImpl client = new UMFuturesClientImpl(account.getApiKey(), account.getApiSecret());
//        parameters.put("symbol", checkSymbolLiner(symbol));
        String s = client.account().positionInformation(parameters);
        log.info("币安查询持仓信息  结果:{}", s);
        List<UMPositionItem> list = JSONArray.parseArray(s).toList(UMPositionItem.class);
        ArrayList<PositionWsData> positions = new ArrayList<>();
        if (positionSide) {
            //双向持仓只拿有持仓数量的处理
            for (UMPositionItem position : list) {
//                if (position.getSymbol().equalsIgnoreCase(checkSymbolLiner(symbol))) {
                //处理仓位
                PositionWsData positionWsItem = new PositionWsData();
                String side = getPositionSideDoubleSide(position);
                positionWsItem.setSize(position.getPositionAmt().abs());
                if (SideType.EMPTY.equalsIgnoreCase(side)
                    || position.getPositionAmt().abs().compareTo(BigDecimal.ZERO) == 0) {
//                    log.info("仓位已平 ｛｝", JSONObject.toJSONString(positionWsItem));
//                        continue;
                    positionWsItem.setClosed(true);
                    continue;
                } else {
                    positionWsItem.setClosed(false);
                }
//                    orderPosition.setFee();
                positionWsItem.setLeverage(position.getLeverage());
                positionWsItem.setExchange(ExchangeType.BINANCE.getName());
                positionWsItem.setSymbol(position.getSymbol().replace("USDT", ""));
                //币安查询资金费接口 https://developers.binance.com/docs/zh-CN/derivatives/portfolio-margin/account/Get-UM-Income-History
//                    orderPosition.setFundingFee(position.getBigDecimal("fundingFee"));
//                    orderPosition.setMgnRatio(position.getBigDecimal("mgnRatio"));
//                    positionWsItem.setBreakEvenPrice(position.getBreakEvenPrice());
                positionWsItem.setUnrealizedProfit(position.getUnRealizedProfit());
                positionWsItem.setProfit(position.getUnRealizedProfit());
                positionWsItem.setAvgPrice(position.getEntryPrice());
                positionWsItem.setLiqPrice(position.getLiquidationPrice());

                positionWsItem.setSide(side);
                positionWsItem.setPosType(PositionType.SWAP);
                positionWsItem.setAccountName(account.getName());
                positionWsItem.setAccountId(account.getId());
                positionWsItem.setUpdateTime(new Date(position.getUpdateTime()));
                positionWsItem.setServerTime(new Date());
                positions.add(positionWsItem);

//                }
            }

        } else {
            //单向持仓
            for (UMPositionItem position : list) {
                //处理仓位
                PositionWsData positionWsItem = new PositionWsData();
                String side = getPositionSideDoubleSide(position);
                positionWsItem.setSize(position.getPositionAmt().abs());
                if (SideType.EMPTY.equalsIgnoreCase(side)
                    || position.getPositionAmt().abs().compareTo(BigDecimal.ZERO) == 0) {
                    log.info("仓位已平 ｛｝", JSONObject.toJSONString(positionWsItem));
//                        continue;
                    positionWsItem.setClosed(true);
                } else {
                    positionWsItem.setClosed(false);
                }
//                    orderPosition.setFee();
                positionWsItem.setLeverage(position.getLeverage());
                positionWsItem.setExchange(ExchangeType.BINANCE.getName());
                positionWsItem.setSymbol(position.getSymbol().replace("USDT", ""));
                //币安查询资金费接口 https://developers.binance.com/docs/zh-CN/derivatives/portfolio-margin/account/Get-UM-Income-History
//                    orderPosition.setFundingFee(position.getBigDecimal("fundingFee"));
//                    orderPosition.setMgnRatio(position.getBigDecimal("mgnRatio"));
//                    positionWsItem.setBreakEvenPrice(position.getBreakEvenPrice());
                positionWsItem.setUnrealizedProfit(position.getUnRealizedProfit());
                positionWsItem.setProfit(position.getUnRealizedProfit());
                positionWsItem.setAvgPrice(position.getEntryPrice());
                positionWsItem.setLiqPrice(position.getLiquidationPrice());
                positionWsItem.setMarginPrice(position.getMaintMargin());

                positionWsItem.setSide(side);
                positionWsItem.setPosType(PositionType.SWAP);
                positionWsItem.setAccountName(account.getName());
                positionWsItem.setAccountId(account.getId());
                positionWsItem.setUpdateTime(new Date(position.getUpdateTime()));
                positionWsItem.setServerTime(new Date());
                positions.add(positionWsItem);
            }

        }


        return positions;
    }
}
