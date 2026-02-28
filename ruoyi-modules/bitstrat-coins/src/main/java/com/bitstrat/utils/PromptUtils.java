package com.bitstrat.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.bitstrat.calc.MarketDataCalcManager;
import com.bitstrat.calc.MarketDataCalcStragety;
import com.bitstrat.calc.constant.DefaultIndicatorType;
import com.bitstrat.constant.CommonConstant;
import com.bitstrat.domain.CoinsAiTask;
import com.bitstrat.domain.MarketData;
import com.bitstrat.domain.TermData;
import com.bitstrat.domain.diy.ExtConfigItem;
import com.bitstrat.domain.diy.ExtUserPromptConfig;
import com.bitstrat.domain.diy.MarketDataPromptRule;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.system.service.ISysClientService;
import org.dromara.system.service.ISysConfigService;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/8 19:17
 * @Content
 */

public class PromptUtils {

    public static String covertPosition(JSONArray positions){
        StringBuilder prompt = new StringBuilder();
        for (Object item : positions) {
            JSONObject position = JSONObject.from(item);


            String promptItem = """
            symbol={symbol} size={size} side={side} leverage={leverage} open={open} stopLoss={stopLoss} takeProfit={takeProfit} unrealizedPL={unrealizedPL} achievedProfits={achievedProfits} totalFee={totalFee} marginAmount={marginSize} createPositionTime={createPositionTime}
            """;
            promptItem = promptItem.replace("{symbol}", getOrDefaultString(position,"symbol","-"));
            promptItem = promptItem.replace("{size}", getOrDefaultString(position,"size","-"));
            promptItem = promptItem.replace("{side}", getOrDefaultString(position,"side","-"));
            promptItem = promptItem.replace("{leverage}", getOrDefaultString(position,"leverage","-"));
            promptItem = promptItem.replace("{open}", getOrDefaultString(position,"open","-"));
            promptItem = promptItem.replace("{stopLoss}", getOrDefaultString(position,"stopLoss","-"));
            promptItem = promptItem.replace("{takeProfit}", getOrDefaultString(position,"takeProfit","-"));
            promptItem = promptItem.replace("{unrealizedPL}", getOrDefaultString(position,"unrealizedPL","-"));
            promptItem = promptItem.replace("{achievedProfits}", getOrDefaultString(position,"achievedProfits","-"));
            promptItem = promptItem.replace("{totalFee}", getOrDefaultString(position,"totalFee","-"));
            promptItem = promptItem.replace("{marginSize}", getOrDefaultString(position,"marginSize","-"));
            promptItem = promptItem.replace("{createPositionTime}", getOrDefaultString(position,"createPositionTime","-"));

            prompt.append(promptItem);
            prompt.append("\n");
        }
        return prompt.toString();
    }
    private static String getOrDefaultString(JSONObject item,String key,String defaultValue){
        String string = item.getString(key);
        return StringUtils.isEmpty(string) ? defaultValue : string;
    }

    public static String convertInterval(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        input = input.trim().toLowerCase();

        // 提取数字和字母
        String num = input.replaceAll("[^0-9]", "");
        String unit = input.replaceAll("[0-9]", "");

        if (num.isEmpty() || unit.isEmpty()) {
            return input; // 格式不合法时原样返回
        }

        String fullUnit;
        switch (unit) {
            case "m":
                fullUnit = "minute";
                break;
            case "h":
                fullUnit = "hour";
                break;
            case "d":
                fullUnit = "day";
                break;
            case "w":
                fullUnit = "week";
                break;
            default:
                fullUnit = unit; // 未知单位
        }

        return num + "-" + fullUnit;
    }


    public static BarSeries getBarSeries(String coin, JSONArray marketCandles) {
        BarSeries series = new BaseBarSeriesBuilder().withName(coin).build();
        List<List<BigDecimal>> kLines = new ArrayList<>();
        for (Object o : marketCandles) {
            List<BigDecimal> kline = new ArrayList<>();
            JSONArray item = (JSONArray) o;
            String time = (String) item.get(0);
            String openPrice = (String) item.get(1);
            String highPrice = (String) item.get(2);
            String lowPrice = (String) item.get(3);
            String closePrice = (String) item.get(4);
            String volume = (String) item.get(5);
            kline.add(new BigDecimal(time));
            kline.add(new BigDecimal(openPrice));
            kline.add(new BigDecimal(highPrice));
            kline.add(new BigDecimal(lowPrice));
            kline.add(new BigDecimal(closePrice));
            kline.add(new BigDecimal(volume));
            kLines.add(kline);
        }
        // 按时间升序排序 (从老到新)
        kLines.sort(Comparator.comparing(o -> o.get(0)));
        // 添加 K 线数据，使用收盘价（close）
        for (List<BigDecimal> kline : kLines) {
            ZonedDateTime startTime = Instant.ofEpochMilli(kline.get(0).longValue()).atZone(ZoneId.systemDefault());
            ZonedDateTime endTime = startTime.plusMinutes(15);
            Duration duration = Duration.between(startTime, endTime);
            double open = kline.get(1).doubleValue();
            double hight = kline.get(2).doubleValue();
            double low = kline.get(3).doubleValue();
            double close = kline.get(4).doubleValue();
            double volume = kline.get(5).doubleValue();
            BaseBar baseBar = new BaseBar(duration, endTime, open, hight, low, close, volume);
            series.addBar(baseBar);
        }
        return series;
    }

    public static List<ExtConfigItem> getUserPromptSetting(String extConfig) {
        List<ExtConfigItem> defaultOptions;
        if(StringUtils.isEmpty(extConfig) || !JSON.isValid(extConfig)){
            //没有配置或者配置错误
            //兜底默认指标
            ISysConfigService bean = SpringUtils.getBean(ISysConfigService.class);
            String s = bean.selectConfigByKey(CommonConstant.DEFUALT_USER_PROMPT);
            if(StringUtils.isEmpty(s)){
                return Collections.emptyList();
            }
            defaultOptions = JSONArray.parseArray(s, ExtConfigItem.class);
            if(CollectionUtils.isNotEmpty(defaultOptions)){
                for (ExtConfigItem defaultOption : defaultOptions) {
                    defaultOption.setIsDefault(true);
                }
            }
            return defaultOptions;
        }else{
            ExtUserPromptConfig javaObject = JSON.toJavaObject(extConfig, ExtUserPromptConfig.class);
            defaultOptions = javaObject.getDefaultOptions();
            List<ExtConfigItem> modify = javaObject.getModify();
            List<ExtConfigItem> items = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(defaultOptions)){
                for (ExtConfigItem defaultOption : defaultOptions) {
                    defaultOption.setIsDefault(true);
                    items.add(defaultOption);
                }
            }
            if(CollectionUtils.isNotEmpty(modify)){
                for (ExtConfigItem extConfigItem : modify) {
                    extConfigItem.setIsDefault(false);
                    items.add(extConfigItem);
                }
            }
            return items;
        }
    }

    public static List<MarketDataPromptRule> initRuleData(List<ExtConfigItem> userPromptSetting, ArrayList<MarketData> marketDatas,boolean hasMiddle) {
        List<MarketDataPromptRule> rules = new ArrayList<>();
        MarketDataCalcManager bean = SpringUtils.getBean(MarketDataCalcManager.class);
        for (MarketData marketData : marketDatas) {
            MarketDataPromptRule marketDataPromptRule = new MarketDataPromptRule();
            marketDataPromptRule.setSymbol(marketData.getSymbol());
            marketDataPromptRule.setMarketData(marketData);
            marketDataPromptRule.setExtConfigItems(userPromptSetting);
            //计算自定义指标
            StringBuilder shortBuilder = new StringBuilder();
            StringBuilder middleBuilder = new StringBuilder();
            StringBuilder longBuilder = new StringBuilder();
            for (ExtConfigItem extConfigItem : userPromptSetting) {
                if (!extConfigItem.getIsDefault()) {
                    //自定义
                    MarketDataCalcStragety<?> stragety = bean.getStragety(extConfigItem.getType());
                    if (Objects.nonNull(stragety)) {
                        String s = stragety.calcPrompt(extConfigItem, marketData.getSeriesShort(), CommonConstant.DEFAULT_LIMIT_SIZE_AI_TASK);
                        shortBuilder.append(s).append("\n");

                        String l = stragety.calcPrompt(extConfigItem, marketData.getSeriesLong(), CommonConstant.DEFAULT_LIMIT_SIZE_AI_TASK);
                        longBuilder.append(l).append("\n");
                        if (hasMiddle) {
                            String m = stragety.calcPrompt(extConfigItem, marketData.getSeriesMiddle(), CommonConstant.DEFAULT_LIMIT_SIZE_AI_TASK);
                            middleBuilder.append(m).append("\n");
                        }


                    }
                }
            }
            marketDataPromptRule.setShortPrompt(shortBuilder.toString());
            marketDataPromptRule.setLongPrompt(longBuilder.toString());
            if (hasMiddle) {
                marketDataPromptRule.setMiddlePrompt(middleBuilder.toString());
            }

            rules.add(marketDataPromptRule);
        }




        return rules;
    }



    public static void generateMiddleTerm(CoinsAiTask coinsAiTask, StringBuilder userPrompt, MarketDataPromptRule symbolRoleData, TermData middleTerm) {
        String dataPathTitle5 = """
            Mid-term context ({middleInterval} timeframe):
            """;
        if (StringUtils.isNoneEmpty(coinsAiTask.getMiddleTermInterval())) {
            dataPathTitle5 = dataPathTitle5.replace("{middleInterval}", PromptUtils.convertInterval(coinsAiTask.getMiddleTermInterval()));
        } else {
            dataPathTitle5 = dataPathTitle5.replace("{middleInterval}", "2-hour");
        }
        userPrompt.append(dataPathTitle5).append("\n");
        if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.EMA20vsEMA50, true)){

            String dataPathLonggerEMA = """
            20‑Period EMA: {20PeriodEMA} vs. 50‑Period EMA: {50PeriodEMA}
            """;
            dataPathLonggerEMA = dataPathLonggerEMA.replace("{20PeriodEMA}", middleTerm.getEma20Value());
            dataPathLonggerEMA = dataPathLonggerEMA.replace("{50PeriodEMA}", middleTerm.getEma50Value());
            userPrompt.append(dataPathLonggerEMA).append("\n");
        }

        if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.ATR3vsATR14, true)) {
            String dataPathATR = """
                3‑Period ATR: {ATR3} vs. 14‑Period ATR: {ATR14}
                """;
            dataPathATR = dataPathATR.replace("{ATR3}", middleTerm.getAtr3().toPlainString());
            dataPathATR = dataPathATR.replace("{ATR14}", middleTerm.getAtr14().toPlainString());
            userPrompt.append(dataPathATR).append("\n");
        }

        if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.CVvsAV, true)){
            String dataPathVolume = """
            Current Volume: {volume} vs. Average Volume: {volumeAVG}
            """;
            dataPathVolume = dataPathVolume.replace("{volume}", middleTerm.getCurrentVolume().toPlainString());
            dataPathVolume = dataPathVolume.replace("{volumeAVG}", middleTerm.getAverageVolume().toPlainString());
            userPrompt.append(dataPathVolume).append("\n");
        }

        if (CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.MACD, true)) {
            String dataPathMACDIndicatorsLong = """
            MACD indicators (12,26): {macdIndicators}
            """;
            dataPathMACDIndicatorsLong = dataPathMACDIndicatorsLong.replace("{macdIndicators}", middleTerm.getMacdIndicatorsString().toString());
            userPrompt.append(dataPathMACDIndicatorsLong).append("\n");
        }

        if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.RSI7, true)){
            String dataPathRSIndicators7Long = """
            RSI indicators (7‑Period): {rsi7period}
            """;
            dataPathRSIndicators7Long = dataPathRSIndicators7Long.replace("{rsi7period}", middleTerm.getRsi7period().toString());
            userPrompt.append(dataPathRSIndicators7Long).append("\n");
        }

        if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.RSI14, true)){
            String dataPathRSIndicators14Long = """
            RSI indicators (14‑Period): {rsi14period}
            """;
            dataPathRSIndicators14Long = dataPathRSIndicators14Long.replace("{rsi14period}", middleTerm.getRsi14period().toString());
            userPrompt.append(dataPathRSIndicators14Long).append("\n");
        }

        if(CalcUtils.checkHasDataByName(symbolRoleData, DefaultIndicatorType.BOLL, true)){
            userPrompt.append(middleTerm.getBollingerBandsValue());
        }

        //------------------------自定义参数 long
        String longPrompt = symbolRoleData.getMiddlePrompt();
        if (StringUtils.isNoneBlank(longPrompt)) {
            userPrompt.append("\n");
            userPrompt.append(longPrompt).append("\n");
        }
    }
}
