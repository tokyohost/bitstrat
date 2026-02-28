package com.bitstrat.domain.diy;

import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/21 21:13
 * @Content
 * type: string;
 *   value: string;
 *   desc: string;
 */

@Data
public class ExtConfigItem {
    Boolean isDefault; //是否预设
    String type;
    String value;
    String desc;

}
