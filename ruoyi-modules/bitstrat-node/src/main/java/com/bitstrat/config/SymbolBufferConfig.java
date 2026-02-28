package com.bitstrat.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "symbol-buffer")
public class SymbolBufferConfig {
   private List<SymbolConfigNode> symbols;

   public SymbolConfigNode getSymbol(String symbol) {
       for (SymbolConfigNode node : symbols) {
           if (node.getSymbol().equalsIgnoreCase(symbol)) {
               return node;
           }
       }
       return null;
   }

}
