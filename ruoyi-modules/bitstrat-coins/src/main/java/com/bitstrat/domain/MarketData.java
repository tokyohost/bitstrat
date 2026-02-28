package com.bitstrat.domain;

import com.bitstrat.domain.vo.SymbolFundingRate;
import lombok.Data;
import org.ta4j.core.BarSeries;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/10/31 16:28
 * @Content
 */

@Data
public class MarketData {

    String symbol;

    String currentPrice;

    String change24H;

    TermData shortTerm;
    TermData middleTermData;
    TermData shortTerm1m;
    TermData longTerm;

    SymbolFundingRate fundingRate;
    BigDecimal openInterest;

    BarSeries seriesShort;
    BarSeries seriesMiddle;
    BarSeries seriesLong;


}
