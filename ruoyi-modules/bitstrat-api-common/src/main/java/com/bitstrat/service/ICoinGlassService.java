package com.bitstrat.service;

import com.bitstrat.domain.coinGlass.CoinFundingInfo;
import com.bitstrat.domain.coinGlass.CoinGlassRundingRate;
import com.bitstrat.domain.coinGlass.SportFundingRateItem;
import org.dromara.common.core.domain.R;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/16 15:27
 * @Content
 */


public interface ICoinGlassService {
    List<CoinFundingInfo> queryFundingRateInterestArbitrage(String ex);


    R querySupportSymbol();

    List<SportFundingRateItem> queryFundingRateSpot();
    public CoinGlassRundingRate queryFundingRateBySymbol(String sourceSymbol);
    public CoinGlassRundingRate queryFundingRateBySymbolInterval(String sourceSymbol,String interval);
    BigDecimal queryFundingRateBySymbol(String sourceSymbol, String exchange);
}
