package com.bitstrat.config;

import com.bitget.openapi.common.client.BitgetRestClient;
import com.bitget.openapi.common.domain.ClientParameter;
import com.bitget.openapi.common.enums.SignTypeEnum;
import com.bitget.openapi.common.enums.SupportedLocaleEnum;
import org.dromara.common.core.utils.StringUtils;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/7 11:02
 * @Content
 */

public class BitgetClientService {
    public static String BASE_URL = "https://api.bitget.com/";

    /***
     *
     * @param apiKey
     * @param secretKey
     * @param passphrase
     * @param signType 加密类型
     * @param paptrading 模拟盘标记
     * @return
     */
    public static BitgetRestClient createClient(String apiKey,String secretKey,String passphrase,String signType,String paptrading){
        return createClient(apiKey,secretKey,passphrase,null,signType,paptrading);
    }
    public static BitgetRestClient createClient(String apiKey,String secretKey,String passphrase,String baseUrl,String signType,String paptrading){
        ClientParameter.ClientParameterBuilder builder = ClientParameter.builder()
            .apiKey(apiKey)
            .secretKey(secretKey) // 如果是RSA类型则设置为RSA私钥
            .passphrase(passphrase);
        if (StringUtils.isEmpty(baseUrl)) {
            builder.baseUrl(BASE_URL);
        }else{
            builder.baseUrl(baseUrl);
        }
        if(StringUtils.isNotEmpty(paptrading)){
            builder.paptrading(paptrading);
        }
        if (SignTypeEnum.RSA.getName().equalsIgnoreCase(signType)) {
            builder.signType(SignTypeEnum.RSA);// 如果你的apikey是RSA类型则主动设置签名类型为RSA
        }
        ClientParameter parameter =builder
            .locale(SupportedLocaleEnum.ZH_CN.getName()).build();
        return BitgetRestClient.builder().configuration(parameter).build();
    }
}
