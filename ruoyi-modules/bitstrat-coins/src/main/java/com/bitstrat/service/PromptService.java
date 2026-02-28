package com.bitstrat.service;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/1 17:12
 * @Content
 */

import com.bitstrat.domain.Account;
import com.bitstrat.domain.CoinContractInfomation;
import com.bitstrat.domain.CoinsAiTask;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.AccountUtils;
import com.bitstrat.utils.PromptUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PromptService {

    @Autowired
    ExchangeApiManager exchangeApiManager;

    @Autowired
    ICoinsApiService coinsApiService;

    @Autowired
    private ResourceLoader resourceLoader;

    @SneakyThrows
    public String loadSystemPrompt(){
        Resource resource = resourceLoader.getResource("classpath:defaultSystemPrompt.md");
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public String formatPrompt(String systemPrompt, CoinsAiTask coinsAiTask) {
        systemPrompt = systemPrompt.replace("#{exchange}", coinsAiTask.getExchange().toUpperCase());
        systemPrompt = systemPrompt.replace("#{assetUniverse}", coinsAiTask.getSymbols().toUpperCase());
        systemPrompt = systemPrompt.replace("#{startCapital}", coinsAiTask.getStartBalance().setScale(2, RoundingMode.HALF_UP).toPlainString());
        systemPrompt = systemPrompt.replace("#{timeRange}", coinsAiTask.getInterval());
        systemPrompt = systemPrompt.replace("#{symbolJson}", coinsAiTask.getSymbols().toUpperCase().replaceAll(",","|"));
        Long leverageMin = 3L;
        Long leverageMax = 20L;
        if (Objects.nonNull(coinsAiTask.getLeverageMin())) {
            leverageMin = coinsAiTask.getLeverageMin();
        }
        if (Objects.nonNull(coinsAiTask.getLeverageMax())) {
            leverageMax = coinsAiTask.getLeverageMax();
        }

        systemPrompt = systemPrompt.replace("#{leverageRange}", leverageMin + "-" + leverageMax);
        systemPrompt = systemPrompt.replace("#{leverageMin}", leverageMin+"");
        systemPrompt = systemPrompt.replace("#{leverageMax}", leverageMax+"");
        systemPrompt = systemPrompt.replace("#{shortInterval}", PromptUtils.convertInterval(coinsAiTask.getShortTermInterval()));

        if (Objects.nonNull(coinsAiTask.getNeedMiddleTerm()) && coinsAiTask.getNeedMiddleTerm() == 1) {
            String middlePrompt = """
            - ~10 recent data points for #{middleInterval} timeframe
            """;
            middlePrompt = middlePrompt.replace("#{middleInterval}", PromptUtils.convertInterval(coinsAiTask.getMiddleTermInterval()));
            systemPrompt.replace("#{middleIntervalText}", middlePrompt);
        }else{
            systemPrompt.replace("#{middleIntervalText}", "");
        }

        systemPrompt = systemPrompt.replace("#{longInterval}", PromptUtils.convertInterval(coinsAiTask.getLongTermInterval()));
        systemPrompt = systemPrompt.replace("#{longIntervalAnalysis}", PromptUtils.convertInterval(coinsAiTask.getLongTermInterval()));
        if (Objects.nonNull(coinsAiTask.getNeedMiddleTerm()) && coinsAiTask.getNeedMiddleTerm() == 1) {
            String middleIntervalExp =  """
                - Use #{middleInterval} data for intermediate trend context and swing support/resistance levels
                """;
            middleIntervalExp = middleIntervalExp.replace("#{middleInterval}", PromptUtils.convertInterval(coinsAiTask.getLongTermInterval()));
            systemPrompt = systemPrompt.replace("#{middleIntervalAnalysis}", middleIntervalExp);
        }else{
            systemPrompt = systemPrompt.replace("#{middleIntervalAnalysis}", "");
        }



        //处理每个币对的步长
        CoinsApiVo coinsApiVo = coinsApiService.queryById(coinsAiTask.getApiId());
        Account account = AccountUtils.coverToAccount(coinsApiVo);
        ExchangeService exchangeService = exchangeApiManager.getExchangeService(coinsAiTask.getExchange());
        String[] symbols = coinsAiTask.getSymbols().split(",");
        ArrayList<String> symbolSteps = new ArrayList<>();

        for (String symbol : symbols) {
            CoinContractInfomation contractCoinInfo = exchangeService.getContractCoinInfo(account, symbol);
            //BTC(0.0001-step)
            BigDecimal step = contractCoinInfo.getStep();
            String stepStr = step.stripTrailingZeros().toPlainString();
            symbolSteps.add(symbol.toUpperCase() + "(" + stepStr + "-step)");

        }
        String symbolStepsStr = String.join("|", symbolSteps);
        systemPrompt = systemPrompt.replace("#{symbolStep}", symbolStepsStr);
        return systemPrompt;
    }
}

