package com.bitstrat.domain.diy;

import lombok.Data;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/21 21:13
 * @Content
 */

@Data
public class ExtUserPromptConfig {
    private List<ExtConfigItem> defaultOptions;
    private List<ExtConfigItem> modify;


}
