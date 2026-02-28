package com.bitstrat.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.domain.coinGlass.CoinFundingInfo;
import com.bitstrat.domain.coinGlass.CoinGlassRundingRate;
import com.bitstrat.domain.coinGlass.SportFundingRateItem;
import com.bitstrat.service.ICoinGlassService;
import com.google.common.collect.Iterables;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/16 15:27
 * @Content coinGlass api 调用
 */

@Service
public class CoinsGlassServiceImpl implements ICoinGlassService {
    @Autowired
    RestTemplate restTemplate;



    @Override
    public List<CoinFundingInfo> queryFundingRateInterestArbitrage(String ex) {
        String url = "https://capi.coinglass.com/api/fundingRate/interestArbitrageV2";
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("ex", ex);

        try {
            String data = "NO API";
            List<CoinFundingInfo> arbitrageItems = JSON.parseArray(data, CoinFundingInfo.class);
            return arbitrageItems;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public R querySupportSymbol() {
        String url = "https://capi.coinglass.com/api/support/symbol";
        HashMap<String, Object> queryParams = new HashMap<>();

        try {
            String data = "NO API";
            List<String> arbitrageItems = JSON.parseArray(data, String.class);
            return R.ok(arbitrageItems);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail();
        }
    }

    @Override
    public List<SportFundingRateItem> queryFundingRateSpot() {

        String url = "https://capi.coinglass.com/api/fundingRate/arbitrage-list?exchangeName=all";
        HashMap<String, Object> queryParams = new HashMap<>();
        try {
            String data = "NO API";
            List<SportFundingRateItem> fundingRateItems = JSON.parseArray(data, SportFundingRateItem.class);
            return fundingRateItems;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    @Cacheable(value = "coinGlassFundingRate:cache#15s#60s#20", key = "'coinGlass'+':'+'fundingRate'+':'+#sourceSymbol",condition = "#sourceSymbol != null ")
    public CoinGlassRundingRate queryFundingRateBySymbol(String sourceSymbol) {
//        String url = "https://capi.coinglass.com/api/fundingRate/v2/history/chart?symbol="+sourceSymbol+"&type=U&interval=m1";
        String url = "https://capi.coinglass.com/api/fundingRate/v2/history/chart";
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("symbol", sourceSymbol);
        queryParams.put("type", "U");
        queryParams.put("interval", "m1");
        try {
            String data = "NO API";
            CoinGlassRundingRate coinGlassRundingRate = JSONObject.parseObject(data, CoinGlassRundingRate.class);


//            return fundingRateItems;
            return coinGlassRundingRate;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
    @Cacheable(value = "coinGlassFundingRateInterval:cache#60s#60s#500", key = "'coinGlass'+':'+'fundingRate'+':'+#sourceSymbol+':'+#interval",condition = "#sourceSymbol != null ")
    public CoinGlassRundingRate queryFundingRateBySymbolInterval(String sourceSymbol,String interval) {
//        String url = "https://capi.coinglass.com/api/fundingRate/v2/history/chart?symbol="+sourceSymbol+"&type=U&interval=m1";
        String url = "https://capi.coinglass.com/api/fundingRate/v2/history/chart";
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("symbol", sourceSymbol);
        queryParams.put("type", "U");
        queryParams.put("interval", interval == null ? "m5" : interval);
        try {
            String data = "NO API";
            CoinGlassRundingRate coinGlassRundingRate = JSONObject.parseObject(data, CoinGlassRundingRate.class);


//            return fundingRateItems;
            return coinGlassRundingRate;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
    @Override
    public BigDecimal queryFundingRateBySymbol(String sourceSymbol, String exchange) {

        CoinGlassRundingRate coinGlassRundingRate = SpringUtils.getBean(this.getClass()).queryFundingRateBySymbol(sourceSymbol);
        //查找交易所
        Long last = Iterables.getLast(coinGlassRundingRate.getDateList(), null);
        if(last!=null){
            Set<String> exlist = coinGlassRundingRate.getFrDataMap().keySet();
            for (String ex : exlist) {
                if(ex.equalsIgnoreCase(exchange)){
                    List<Double> feeList = coinGlassRundingRate.getFrDataMap().getOrDefault(ex, new ArrayList<>());
                    Double feeNew = Iterables.getLast(feeList, null);
                    return BigDecimal.valueOf(feeNew);
                }
            }

        }

//            return fundingRateItems;
        return null;
    }
}
