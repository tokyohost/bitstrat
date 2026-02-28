package com.bitstrat.service;

import com.bitstrat.domain.AiStreamQuery;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/8 19:54
 * @Content
 */

public interface AiSereamService {
    String queryKLinePrompt(@NotEmpty(message = "请选择交易所") String exchange, @NotEmpty(message = "请选择币对") List<String> symbol,
         AiStreamQuery aiStreamQuery);
}
