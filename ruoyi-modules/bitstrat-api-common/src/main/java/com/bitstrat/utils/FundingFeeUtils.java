package com.bitstrat.utils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class FundingFeeUtils {
    public static BigDecimal checkFunding(BigDecimal longFundingFee, BigDecimal shortFundingFee) {
        //核心逻辑，资金费是正数，做多一方需要按照持仓数量按照资金费比例支付资金费给做空一方，资金费是负数，做空一方需要按照持仓数量和资金费比例支付给做多一方
        //如果做多资金费 负数 做空资金费 正数，属于两头纯赚
        log.info("longFundingFee {} shortFundingFee:{} ", longFundingFee, shortFundingFee);
        if (longFundingFee.doubleValue() < 0
            && shortFundingFee.doubleValue() > 0) {
            //纯赚
            return longFundingFee.abs().add(shortFundingFee);
        }

        // 做多资金费变正数(需要付资金费了) 做空资金费还是正数，做多一方付的资金费率比做空一方收到的资金费少(还有盈利空间)
        // 计算方式(示例 做多0.01% 需要支付0.01%资金费，做空0.02% 会收到0.02%的资金费，那么就是0.02 - 0.01 还有0.01% 的利润空间)
        if (longFundingFee.doubleValue() >= 0
            && shortFundingFee.doubleValue() >= 0) {
            if (shortFundingFee.doubleValue() > longFundingFee.doubleValue()) {

                //还有利润空间
                return shortFundingFee.subtract(longFundingFee);
            } else//两边资金费相等(没有利润空间了支付的和收到的一样了)
                if (shortFundingFee.doubleValue() == longFundingFee.doubleValue()) {
                    return shortFundingFee.subtract(longFundingFee);
                }
                else//做空比做多资金费大，那么没有利润空间
                    if(shortFundingFee.doubleValue() < longFundingFee.doubleValue()) {
                        return shortFundingFee.subtract(longFundingFee);
                    }

        }

        if (longFundingFee.doubleValue() < 0
            && shortFundingFee.doubleValue() < 0) {
            if (shortFundingFee.doubleValue() > longFundingFee.doubleValue()) {

                //还有利润空间
                return longFundingFee.add(shortFundingFee.abs()).abs();
            } else//两边资金费相等(没有利润空间了支付的和收到的一样了)
                if (shortFundingFee.doubleValue() == longFundingFee.doubleValue()) {
                    return BigDecimal.ZERO;
                }
                else
                if(shortFundingFee.doubleValue() < longFundingFee.doubleValue()) {
                    return shortFundingFee.add(longFundingFee.abs());
                }

        }
        //资金费完全倒挂 做多方资金费变正数，做空方资金费变负数，完全倒挂多空都要支付！
        if(longFundingFee.doubleValue() > 0
            && shortFundingFee.doubleValue() < 0) {

            return longFundingFee.abs().add(shortFundingFee.abs()).negate();
        }
        return BigDecimal.ZERO;
    }
}
