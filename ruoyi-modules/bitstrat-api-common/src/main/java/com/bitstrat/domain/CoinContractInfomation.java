package com.bitstrat.domain;

import com.bitstrat.constant.binance.BinanceUMFilterType;
import com.bitstrat.domain.binance.Filter;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 合约详情
 */
@Data
public class CoinContractInfomation {

    BigDecimal maxMktSz;//单笔最大市价单委托数量
    BigDecimal maxLmtSz;//单笔最大限价单委托数量
    BigDecimal minSz; //最小委托数量（以对应币为单位）
    Integer calcPlaces;//计算小数位数 (数量的小数位数)

    Integer maxLeverage; //最大杠杆数
    Integer minLeverage;//最小杠杆数
    BigDecimal contractValue;//合约面值  10 就是10个对应的币对数量
    BigDecimal step;//下单步长
    BigDecimal ctMult;//合约乘数  1 就是合约面值* 合约乘数 一个合约等于 合约面值* 合约乘数  1张 = 1 * 10 = 10 PROMPT
    String symbol; //币种名称
    BigDecimal price; //当前价格

    BigDecimal okxMinSz; //okx最小委托数量（以张为单位，最小多少张）
    /**
     * 资金费结算周期，bybit 会有
     */
    BigDecimal fundingInterval;

    /**
     * 最小开单价格 bitget / binance
     */
    BigDecimal minTradeUSDT;
    /**
     * 数量乘数 下单数量要大于 minTradeNum 并且满足 sizeMultiplier 的倍数
     * bitget only
     */
    BigDecimal sizeMultiplier;


    /**
     * 价格步长
     * bitget only
     */
    BigDecimal priceEndStep;
    /**
     * 小数位数
     * bitget only / binance
     */
    BigDecimal pricePlace;

    /**
     * 下单价格偏移比例 上限 binance
     * 1.15  = 115%
     */
    BigDecimal multiplierUp;
    /**
     * 下单价格偏移比例 下限 binance
     * 0.85  = 85%
     */
    BigDecimal multiplierDown;


    /**
     * 币安下单价格数量过滤器
     * see {@link  BinanceUMFilterType}
     */
    List<Filter> filters;
}
