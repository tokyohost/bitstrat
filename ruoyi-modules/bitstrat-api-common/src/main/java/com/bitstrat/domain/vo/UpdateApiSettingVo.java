package com.bitstrat.domain.vo;

import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/18 16:06
 * @Content
 *  exchange: currentExchange.value?.exchangeName,
 *     apiKey: configForm.value.apiKey,
 *     apiSecurity: configForm.value.secret,
 *     passphrase: configForm.value.passphrase
 */

@Data
public class UpdateApiSettingVo {
    String exchange;
    String apiKey;
    String apiSecurity;
    String passphrase;
    String type;


}
