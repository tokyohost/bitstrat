package com.bitstrat.domain.msg;

import com.bitstrat.domain.server.MessageData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/10 18:12
 * @Content 监听实时行情数据
 */

public class SubscribeSymbol implements MessageData {
    List<String> symbols;
    String symbolType;

    public List<String> getSymbols() {
        return symbols;
    }

    public void setSymbols(List<String> symbols) {
        this.symbols = symbols;
    }

    public String getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(String symbolType) {
        this.symbolType = symbolType;
    }
}
