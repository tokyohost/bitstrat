package com.bitstrat.ai.utils;

import com.bitstrat.ai.constant.BusinessType;
import com.bitstrat.ai.domain.CompareContext;
import com.bitstrat.ai.domain.CompareItem;
import com.bitstrat.ai.domain.StartCompareContext;
import com.bitstrat.constant.ExchangeType;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.bitstrat.ai.constant.BusinessType.BUSINESS_TYPE;
import static com.bitstrat.ai.constant.BusinessType.COMPARE;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/28 17:35
 * @Content
 */

public class BusinessTypeProcess {
    public static CompareContext processBusinessType(String clientId, Map<String, String> paramMap) {
        CompareContext compareContext = new CompareContext();
        compareContext.setClientId(clientId);
        if (!paramMap.containsKey(BUSINESS_TYPE)) {
            throw new RuntimeException("unknown business type");
        }
        String businessType = paramMap.get(BUSINESS_TYPE);
        switch (businessType) {
            case COMPARE:
                //市价比对
                return handleCompare(compareContext, paramMap);
        }

        return null;
    }

    /**
     * 处理市价比对
     * @param compareContext
     * @param paramMap
     * @return
     */
    private static CompareContext handleCompare(CompareContext compareContext, Map<String, String> paramMap) {
        String exchangeParamPrifex = BusinessType.COMPARE_EXCHANGE_PARAMS;
        String symbolParamPrifex = BusinessType.COMPARE_SYMBOL_PARAMS;
        String typeParamPrifex = BusinessType.COMPARE_TYPE_PARAMS;

        List<CompareItem> items = new ArrayList<>();
        //最大支持10个市价对比
        for (int i = 1; i <= 10; i++) {
            String exchange = paramMap.get(exchangeParamPrifex + "" + i);
            String symbol = paramMap.get(symbolParamPrifex + "" + i);
            String type = paramMap.get(typeParamPrifex + "" + i);
            if(StringUtils.isNotEmpty(exchange)
            && StringUtils.isNotEmpty(symbol)
            && StringUtils.isNotEmpty(type)) {
                ExchangeType exchangeType = ExchangeType.getExchangeType(exchange);
                if (Objects.isNull(exchangeType)) {
                    throw new RuntimeException("unSupport exchange type");
                }
                CompareItem compareItem = new CompareItem();
                if(type.equalsIgnoreCase(BusinessType.COMPARE_TYPE_SWAP)) {
                    compareItem.setType(BusinessType.COMPARE_TYPE_SWAP);
                }else if(type.equalsIgnoreCase(BusinessType.COMPARE_TYPE_SPOT)) {
                    compareItem.setType(BusinessType.COMPARE_TYPE_SPOT);
                }else{
                    throw new RuntimeException("unSupport compare type");
                }
                compareItem.setExchange(exchangeType.getName());
                compareItem.setSymbol(symbol);
                items.add(compareItem);
            }else{
                break;
            }
        }
        if(CollectionUtils.isEmpty(items)) {
            //空的对比
            throw new RuntimeException("no compare items found");
        }
        compareContext.setCompareList(items);
        compareContext.setType(BusinessType.COMPARE);


        return compareContext;
    }

    public static StartCompareContext coverToStartContext(CompareContext compareContext) {
        StartCompareContext startCompareContext = new StartCompareContext();
        BeanUtils.copyProperties(compareContext, startCompareContext);
        return startCompareContext;
    }
}
