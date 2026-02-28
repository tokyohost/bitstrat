// YApi QuickType插件生成，具体参考文档:https://plugins.jetbrains.com/plugin/18847-yapi-quicktype/documentation

package com.bitstrat.domain;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class HistoryPositionTpslQuery {
    @NotEmpty(message = "exchange  cannot be empty")
    private String exchange;
    @NotNull(message = "apiId  cannot be empty")
    private Long apiId;

    private String symbol;
}
