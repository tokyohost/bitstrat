package com.bitstrat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.coinGlass.ArbitrageItem;
import com.bitstrat.domain.coinGlass.CoinFundingInfo;
import com.bitstrat.domain.coinGlass.SportFundingRateItem;
import com.bitstrat.service.ICoinGlassService;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/16 15:25
 * @Content
 */

@RestController
@RequestMapping("/analysis")
public class CoinsAnalysisController {

    @Autowired
    ICoinGlassService coinGlassService;


    @GetMapping("/interestArbitrageV2")
    @SaCheckLogin
    public R interestArbitrageV2(String ex) {
        if (StringUtils.isEmpty(ex)) {
            ex = Arrays.stream(ExchangeType.values()).map(ExchangeType::getCoinsGlassQuery).collect(Collectors.joining(","));
        }
        List<CoinFundingInfo> coinFundingInfoList = coinGlassService.queryFundingRateInterestArbitrage(ex);
        return R.ok(coinFundingInfoList);
    }
    @GetMapping("/fundingRateSpot")
    @SaCheckLogin
    public R fundingRateSpot() {
        List<SportFundingRateItem> coinFundingInfoList = coinGlassService.queryFundingRateSpot();
        return R.ok(coinFundingInfoList);
    }

    /**
     * 查自己那个交易所的指定币种余额
     * @return
     */
    @SaCheckLogin
    @GetMapping("/querySupportSymbol")
    public R querySupportSymbol() {


        return coinGlassService.querySupportSymbol();
    }
    /**
     * 查自己那个交易所的指定币种余额
     * @return
     */
    @SaCheckLogin
    @GetMapping("/queryFundingRateBySymbolInterval")
    public R queryFundingRateBySymbolInterval(String symbol,String interval) {
        if (StringUtils.isEmpty(interval)) {
            return R.ok(coinGlassService.queryFundingRateBySymbolInterval(symbol,"m5"));
        }
        return R.ok(coinGlassService.queryFundingRateBySymbolInterval(symbol,interval));
    }

}
