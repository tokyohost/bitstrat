package org.dromara.test;

import com.bitstrat.utils.FundingFeeUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class FundingTest {

    @Test
    public void test() {
        BigDecimal longFundFee = BigDecimal.valueOf(0.01);
        BigDecimal shortFundFee = BigDecimal.valueOf(0.01);
        //预期是0
        BigDecimal result = FundingFeeUtils.checkFunding(longFundFee, shortFundFee);
        Assertions.assertEquals(0, result.compareTo(BigDecimal.ZERO));


    }
    @Test
    public void test1() {
        BigDecimal longFundFee = BigDecimal.valueOf(0.01);
        BigDecimal shortFundFee = BigDecimal.valueOf(0);
        //预期是负的-0.01 支付了0.01 但是没有对冲收到
        BigDecimal result = FundingFeeUtils.checkFunding(longFundFee, shortFundFee);
        Assertions.assertEquals(-0.01, result.doubleValue());


    }
    @Test
    public void test2() {
        BigDecimal longFundFee = BigDecimal.valueOf(0.01);
        BigDecimal shortFundFee = BigDecimal.valueOf(0.02);
        //预期是0.01
        BigDecimal result = FundingFeeUtils.checkFunding(longFundFee, shortFundFee);
        Assertions.assertEquals(0.01, result.doubleValue());


    }
    @Test
    public void test3() {
        BigDecimal longFundFee = BigDecimal.valueOf(0.02);
        BigDecimal shortFundFee = BigDecimal.valueOf(0.01);
        //预期是0.01
        BigDecimal result = FundingFeeUtils.checkFunding(longFundFee, shortFundFee);
        Assertions.assertEquals(-0.01, result.doubleValue());


    }
    @Test
    public void test4() {
        BigDecimal longFundFee = BigDecimal.valueOf(-0.02);
        BigDecimal shortFundFee = BigDecimal.valueOf(-0.01);
        //预期是0.01
        BigDecimal result = FundingFeeUtils.checkFunding(longFundFee, shortFundFee);
        Assertions.assertEquals(0.01, result.doubleValue());

    }
    @Test
    public void test5() {
        BigDecimal longFundFee = BigDecimal.valueOf(-0.02);
        BigDecimal shortFundFee = BigDecimal.valueOf(0.01);
        //预期是0.03
        BigDecimal result = FundingFeeUtils.checkFunding(longFundFee, shortFundFee);
        Assertions.assertEquals(0.03, result.doubleValue());

    }
    @Test
    public void test6() {
        BigDecimal longFundFee = BigDecimal.valueOf(0.02);
        BigDecimal shortFundFee = BigDecimal.valueOf(-0.01);
        //预期是0.03
        BigDecimal result = FundingFeeUtils.checkFunding(longFundFee, shortFundFee);
        Assertions.assertEquals(-0.03, result.doubleValue());

    }
    @Test
    public void test7() {
        BigDecimal longFundFee = BigDecimal.valueOf(0);
        BigDecimal shortFundFee = BigDecimal.valueOf(0);
        //预期是0.03
        BigDecimal result = FundingFeeUtils.checkFunding(longFundFee, shortFundFee);
        Assertions.assertEquals(0, result.doubleValue());

    }
}
