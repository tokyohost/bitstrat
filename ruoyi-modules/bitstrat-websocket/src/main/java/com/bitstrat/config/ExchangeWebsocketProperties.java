package com.bitstrat.config;


import com.bitstrat.constant.ApiTypeConstant;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.utils.APITypeHelper;
import com.bitstrat.wsClients.constant.WebSocketType;
import lombok.Data;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/28 16:28
 * @Content
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "exchange-websocket")
public class ExchangeWebsocketProperties {

    private List<ExchangeConfig> exchanges;

    /**
     *
     * @param ex {@link ExchangeType}
     * @param type {@link WebSocketType}
     * @return
     */
    public String getUrlByExAndType(String ex, String type) {
        String apiType = APITypeHelper.peek();
        Map<String, ExchangeConfig> configMap = exchanges.stream().collect(Collectors.toMap(item->item.getEx().toLowerCase(), item -> item));
        if(configMap.containsKey(ex.toLowerCase())) {
            String urlByType = getUrlByType(configMap.get(ex.toLowerCase()), type,apiType);
            return urlByType;
        }

        return null;
    }

    /**
     *
     * @param config
     * @param type {@link WebSocketType}
     * @return
     */
    public String getUrlByType(ExchangeConfig config, String type, String apiType) {
        if(WebSocketType.PRIVATE.equalsIgnoreCase(type)) {
            return checkApiType(apiType,config.getPrivateUrl(),config.getPapPrivateUrl());
        }else if(WebSocketType.LINER.equalsIgnoreCase(type)) {
            return checkApiType(apiType,config.getPublicLinear(),config.getPublicLinear());
        }else if(WebSocketType.TRADE.equalsIgnoreCase(type)) {
            return checkApiType(apiType,config.getTrade(),config.getTrade());
        }else if(WebSocketType.SPOT.equalsIgnoreCase(type)) {
            return checkApiType(apiType,config.getPublicSpot(),config.getPublicSpot());
        }else if(WebSocketType.PUBLIC.equalsIgnoreCase(type)) {
            return checkApiType(apiType,config.getPublicUrl(),config.getPapPublicUrl());
        }
        return null;
    }

    private String checkApiType(String apiType, String privateUrl, String papPrivateUrl) {
        if (StringUtils.isNotEmpty(apiType)) {
            if (ApiTypeConstant.TEST.equalsIgnoreCase(apiType)) {
                return papPrivateUrl;
            }
            if(ApiTypeConstant.PRO.equalsIgnoreCase(apiType)) {
                return privateUrl;
            }
        }
        return papPrivateUrl;
    }
}
