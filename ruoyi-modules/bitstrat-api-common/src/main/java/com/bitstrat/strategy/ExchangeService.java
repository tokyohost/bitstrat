package com.bitstrat.strategy;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.CrossOrderStatus;
import com.bitstrat.domain.*;
import com.bitstrat.domain.bitget.CreateTpSlOnce;
import com.bitstrat.domain.bitget.TickerItem;
import com.bitstrat.domain.bitget.UpdateTpSl;
import com.bitstrat.domain.vo.SymbolFundingRate;
import com.bitstrat.wsClients.msg.SubscriptMsgs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/17 14:42
 * @Content
 */
public interface ExchangeService {

    public SubscriptMsgs getWsSubscriptMsgs();
    public String getExchangeName();

    public CoinContractInfomation getContractCoinInfo(Account account,String symbol);
    /**
     * 设置杠杆倍数
     * @param leverage
     */
    public String setLeverage(Account account,Integer leverage,String symbol,String side);

    /**
     * 检查凭据是否正确
     * @param account
     * @return
     */
    public boolean checkApi(Account account);
    /**
     * 下合约买单
     * @param params
     */
    public OrderOptStatus buyContract(Account account,OrderVo params);
    /**
     * 下合约卖单
     * @param params
     */
    public OrderOptStatus sellContract(Account account,OrderVo params);
    /**
     * 下现货买单
     * @param params
     */
    public String buySpot(Account account,JSONObject params);
    /**
     * 下现货卖单
     * @param params
     */
    public String sellSpot(Account account,JSONObject params);

    /**
     * 检查合约状态
     * @param params
     */
    public String checkContractOrder(Account account, OrderInfo orderInfo);
    /**
     * 检查现货状态
     * @param params
     */
    public void checkSpotOrder(Account account,JSONObject params);
    /**
     * 取消现货单，如果已成交，则平仓
     * @param params
     */
    public void cancelSpotOrder(Account account,OrderInfo params);
    /**
     * 取消合约单，如果已成交，则平仓
     * @param params
     */
    public String cancelContractOrder(Account account,OrderOptStatus order);

    /**
     * 合约平仓
     * @param account
     * @param order
     * @return
     */
    public OrderCloseResult closeContractPosition(Account account, OrderPosition order);

    /**
     * 查询合约持仓
     * @param account
     * @param symbol
     * @return
     */
    public OrderPosition queryContractPosition(Account account,String symbol,PositionParams params);

    /**
     * 查询平仓盈亏
     * @param account
     * @param symbol
     * @param params
     * @return
     */
    public BigDecimal queryClosePositionProfit(Account account,String symbol,PositionParams params);

    /**
     * 查询币对的当前资金费
     * @param symbol
     * @return
     */
    public SymbolFundingRate getSymbolFundingRate(String symbol);

    public AccountBalance getBalance(Account account, String coin);

    /**
     * 获取手续费
     * @param account
     * @param coin
     * @return
     */
    public SymbolFee getFee(Account account, String coin);

    /**
     * 获取最新价格
     * @param account
     * @param symbol
     * @return
     */
    public BigDecimal getNowPrice(Account account,String symbol);

    public TickerItem getNowPrice(Account account, String symbol,String bitgetOnly);


    /**
     * 计算真实下单数量
     * @param longOrder
     * @return
     */
    OrderVo calcOrderSize(OrderVo longOrder);
    /**
     * 计算真实下单数量
     * @return
     */
    BigDecimal calcOrderSize(String symbol,BigDecimal size);

    /**
     * 回显币种数量，有的数量可能是张，需要根据不同交易所回显成具体的币种数量
     * @param symbol
     * @param size
     * @return
     */
    BigDecimal calcShowSize(String symbol, BigDecimal size);

    /**
     * 查询订单状态
     * @param account
     * @param orderId
     * @return see {@link CrossOrderStatus}  主要中间态和结束态
     */
    OrderOptStatus queryContractOrderStatus(Account account, OrderOptStatus orderId);

    /**
     * 查询合约订单状态
     * @param account
     * @param orderIds
     * @param symbol
     * @return
     */
    List<ContractOrder> queryContractOrdersByIds(Account account, List<String> orderIds,String symbol);

    /**
     * 改价
     * @param vo
     */
    OrderVo updateContractOrder(Account account,OrderVo vo);

    /**
     * 提前检查订单合法性，比如bybit 开仓每单至少5USDT
     * @param longOrder
     */
    void preCheckOrder(OrderVo longOrder);

    /**
     * 查询所有可用的合约币对
     * @return
     */
    List<LinerSymbol> getAllLinerSymbol();

    /**
     * 查询合约资金费结算时间间隔
     * @return
     */
    Integer getLinerSymbolFundingRateInterval(String symbol);

    /**
     * 订单如果是同步返回的情况下进行formate  binance 会有同步返回订单情况
     * @param syncOrderDetail
     * @return
     */
    ContractOrder formateOrderBySyncOrderInfo(OrderOptStatus orderStatus,Account account,SyncOrderDetail syncOrderDetail);

    public List<PositionWsData> queryContractPositionDetail(Account account, PositionParams params);

    default List< ? extends TpSlOrder> queryContractTpSlOrder(Account account, String symbol) {
        return new ArrayList<>();
    };

    default void updateContractTpSl(Account account, String symbol, TpSlOrder updateTpSl){
        return;
    };

    default void createTpSl(Account account, String symbol, CreateTpSlOnce createTpSlOnce){

    };

    default JSONArray getMarketCandles(Account account, String symbol,String granularity,Long limit){
        return new JSONArray();
    }


    default BigDecimal getOpenInterest(Account account, String symbol) {
        return BigDecimal.ZERO;
    }

    default List<HistoryPosition> queryContractHistoryPosition(Account account,Long size,HistoryPositionQuery query){
        return new ArrayList<>();
    }

    /**
     * 获取仓位唯一ID
     * @param historyPosition
     * @return
     */
    default String getPositionId(HistoryPosition historyPosition) {
        return null;
    }
}
