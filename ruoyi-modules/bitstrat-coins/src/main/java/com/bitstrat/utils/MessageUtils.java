package com.bitstrat.utils;

import com.bitstrat.constant.SideType;
import com.bitstrat.domain.AIOperateItem;
import com.bitstrat.domain.Reasoning;
import com.bitstrat.domain.vo.CoinsAiTaskVo;
import com.bitstrat.service.ICoinsNotifyService;
import org.dromara.common.core.utils.I18nDateTimeUtil;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class MessageUtils {


    public static void sendLongMsg(AIOperateItem aiOperate) {
        I18nDateTimeUtil i18nDateTimeUtil = SpringUtils.getBean(I18nDateTimeUtil.class);
        ICoinsNotifyService bean = SpringUtils.getBean(ICoinsNotifyService.class);

        String side = SideType.LONG.toUpperCase();
        BigDecimal size = aiOperate.getSize();
        String symbol = aiOperate.getSymbol();
        BigDecimal leverage = aiOperate.getLeverage();
        Reasoning reasoning = aiOperate.getReasoning();
        String zh = reasoning.getZh();
        BigDecimal takeProfit = aiOperate.getTakeProfit();
        BigDecimal stopLoss = aiOperate.getStopLoss();
        CoinsAiTaskVo taskVo = aiOperate.getTaskVo();
        String taskName = taskVo.getName();
        String exchange = "-";
        if (StringUtils.isNotEmpty(taskVo.getExchange())) {
            exchange = taskVo.getExchange().toUpperCase();
        }
        LocalDateTime localDateTime = LocalDateTime.now();
        Locale locale = LocaleContextHolder.getLocale();
        String createTimeFormat = i18nDateTimeUtil.format(localDateTime, locale);
        //帮我组成tg 通知消息体
        String msg = """
            🚀 *多头信号触发（LONG）*
            ———————————————
            📌 *策略名称*: %s
            📈 *交易所*: %s
            💱 *交易对*: %s
            📊 *杠杆*: %sx
            📦 *开仓数量*: %s

            🎯 *止盈*: %s
            🛑 *止损*: %s

            🧠 *策略逻辑说明:*
            %s

            ⏰ *触发时间*: %s
            ———————————————
            """.formatted(
            taskName,
            exchange,
            symbol,
            leverage.stripTrailingZeros().toPlainString(),
            size.stripTrailingZeros().toPlainString(),
            takeProfit.stripTrailingZeros().toPlainString(),
            stopLoss.stripTrailingZeros().toPlainString(),
            zh,
            createTimeFormat
        );

        bean.sendNotification(aiOperate.getUserId(), msg);
    }
    public static void sendShortMsg(AIOperateItem aiOperate) {
        I18nDateTimeUtil i18nDateTimeUtil = SpringUtils.getBean(I18nDateTimeUtil.class);
        ICoinsNotifyService bean = SpringUtils.getBean(ICoinsNotifyService.class);

        String side = SideType.SHORT.toUpperCase();
        BigDecimal size = aiOperate.getSize();
        String symbol = aiOperate.getSymbol();
        BigDecimal leverage = aiOperate.getLeverage();
        Reasoning reasoning = aiOperate.getReasoning();
        String zh = reasoning.getZh();
        BigDecimal takeProfit = aiOperate.getTakeProfit();
        BigDecimal stopLoss = aiOperate.getStopLoss();
        CoinsAiTaskVo taskVo = aiOperate.getTaskVo();
        String taskName = taskVo.getName();
        String exchange = "-";
        if (StringUtils.isNotEmpty(taskVo.getExchange())) {
            exchange = taskVo.getExchange().toUpperCase();
        }
        LocalDateTime localDateTime = LocalDateTime.now();
        Locale locale = LocaleContextHolder.getLocale();
        String createTimeFormat = i18nDateTimeUtil.format(localDateTime, locale);
        //帮我组成tg 通知消息体
        String msg = """
            🚀 *空头信号触发（SHORT）*
            ———————————————
            📌 *策略名称*: %s
            📈 *交易所*: %s
            💱 *交易对*: %s
            📊 *杠杆*: %sx
            📦 *开仓数量*: %s

            🎯 *止盈*: %s
            🛑 *止损*: %s

            🧠 *策略逻辑说明:*
            %s

            ⏰ *触发时间*: %s
            ———————————————
            """.formatted(
            taskName,
            exchange,
            symbol,
            leverage.stripTrailingZeros().toPlainString(),
            size.stripTrailingZeros().toPlainString(),
            takeProfit.stripTrailingZeros().toPlainString(),
            stopLoss.stripTrailingZeros().toPlainString(),
            zh,
            createTimeFormat
        );

        bean.sendNotification(aiOperate.getUserId(), msg);
    }
    public static void sendReduceMsg(AIOperateItem aiOperate) {
        I18nDateTimeUtil i18nDateTimeUtil = SpringUtils.getBean(I18nDateTimeUtil.class);
        ICoinsNotifyService bean = SpringUtils.getBean(ICoinsNotifyService.class);

        String side = SideType.SHORT.toUpperCase();
        BigDecimal size = aiOperate.getSize();
        String symbol = aiOperate.getSymbol();
        BigDecimal leverage = aiOperate.getLeverage();
        Reasoning reasoning = aiOperate.getReasoning();
        String zh = reasoning.getZh();
        BigDecimal takeProfit = aiOperate.getTakeProfit();
        BigDecimal stopLoss = aiOperate.getStopLoss();
        CoinsAiTaskVo taskVo = aiOperate.getTaskVo();
        String taskName = taskVo.getName();
        String exchange = "-";
        if (StringUtils.isNotEmpty(taskVo.getExchange())) {
            exchange = taskVo.getExchange().toUpperCase();
        }
        LocalDateTime localDateTime = LocalDateTime.now();
        Locale locale = LocaleContextHolder.getLocale();
        String createTimeFormat = i18nDateTimeUtil.format(localDateTime, locale);
        //帮我组成tg 通知消息体
        String msg = """
            🚀 *减仓信号触发（REDUCE）*
            ———————————————
            📌 *策略名称*: %s
            📈 *交易所*: %s
            💱 *交易对*: %s
            📊 *杠杆*: %sx
            📦 *开仓数量*: %s

            🎯 *止盈*: %s
            🛑 *止损*: %s

            🧠 *策略逻辑说明:*
            %s

            ⏰ *触发时间*: %s
            ———————————————
            """.formatted(
            taskName,
            exchange,
            symbol,
            leverage.stripTrailingZeros().toPlainString(),
            size.stripTrailingZeros().toPlainString(),
            takeProfit.stripTrailingZeros().toPlainString(),
            stopLoss.stripTrailingZeros().toPlainString(),
            zh,
            createTimeFormat
        );

        bean.sendNotification(aiOperate.getUserId(), msg);
    }
    public static void sendClose(AIOperateItem aiOperate) {
        I18nDateTimeUtil i18nDateTimeUtil = SpringUtils.getBean(I18nDateTimeUtil.class);
        ICoinsNotifyService bean = SpringUtils.getBean(ICoinsNotifyService.class);

        String side = SideType.SHORT.toUpperCase();
        BigDecimal size = aiOperate.getSize();
        String symbol = aiOperate.getSymbol();
        BigDecimal leverage = aiOperate.getLeverage();
        Reasoning reasoning = aiOperate.getReasoning();
        String zh = reasoning.getZh();
        BigDecimal takeProfit = aiOperate.getTakeProfit();
        BigDecimal stopLoss = aiOperate.getStopLoss();
        CoinsAiTaskVo taskVo = aiOperate.getTaskVo();
        String taskName = taskVo.getName();
        String exchange = "-";
        if (StringUtils.isNotEmpty(taskVo.getExchange())) {
            exchange = taskVo.getExchange().toUpperCase();
        }
        LocalDateTime localDateTime = LocalDateTime.now();
        Locale locale = LocaleContextHolder.getLocale();
        String createTimeFormat = i18nDateTimeUtil.format(localDateTime, locale);
        //帮我组成tg 通知消息体
        String msg = """
            🚀 *平仓信号触发（CLOSE）*
            ———————————————
            📌 *策略名称*: %s
            📈 *交易所*: %s
            💱 *交易对*: %s

            🧠 *策略逻辑说明:*
            %s

            ⏰ *操作时间*: %s
            ———————————————
            """.formatted(
            taskName,
            exchange,
            symbol,

            zh,
            createTimeFormat
        );

        bean.sendNotification(aiOperate.getUserId(), msg);
    }
    public static void sendTpSl(AIOperateItem aiOperate) {
        I18nDateTimeUtil i18nDateTimeUtil = SpringUtils.getBean(I18nDateTimeUtil.class);
        ICoinsNotifyService bean = SpringUtils.getBean(ICoinsNotifyService.class);

        String side = SideType.SHORT.toUpperCase();
        BigDecimal size = aiOperate.getSize();
        String symbol = aiOperate.getSymbol();
        BigDecimal leverage = aiOperate.getLeverage();
        Reasoning reasoning = aiOperate.getReasoning();
        String zh = reasoning.getZh();
        BigDecimal takeProfit = aiOperate.getTakeProfit();
        BigDecimal stopLoss = aiOperate.getStopLoss();
        CoinsAiTaskVo taskVo = aiOperate.getTaskVo();
        String taskName = taskVo.getName();
        String exchange = "-";
        if (StringUtils.isNotEmpty(taskVo.getExchange())) {
            exchange = taskVo.getExchange().toUpperCase();
        }
        LocalDateTime localDateTime = LocalDateTime.now();
        Locale locale = LocaleContextHolder.getLocale();
        String createTimeFormat = i18nDateTimeUtil.format(localDateTime, locale);
        //帮我组成tg 通知消息体
        String msg = """
            🚀 *调整止盈止损线（TPSL）*
            ———————————————
            📌 *策略名称*: %s
            📈 *交易所*: %s
            💱 *交易对*: %s
            🎯 *新止盈*: %s
            🛑 *新止损*: %s
            🧠 *策略逻辑说明:*
            %s

            ⏰ *操作时间*: %s
            ———————————————
            """.formatted(
            taskName,
            exchange,
            symbol,
            takeProfit,
            stopLoss,
            zh,
            createTimeFormat
        );

        bean.sendNotification(aiOperate.getUserId(), msg);
    }
}
