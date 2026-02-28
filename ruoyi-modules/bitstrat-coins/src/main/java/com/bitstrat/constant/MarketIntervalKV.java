package com.bitstrat.constant;

import com.bybit.api.client.domain.market.MarketInterval;
import org.dromara.common.core.utils.MessageUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/2 14:46
 * @Content
 */

public enum MarketIntervalKV {

    ONE_MINUTE("1","1分钟",TimeUnit.MINUTES,1L),
    THREE_MINUTES("3","3分钟",TimeUnit.MINUTES,3L),
    FIVE_MINUTES("5","5分钟",TimeUnit.MINUTES,5L),
    FIFTEEN_MINUTES("15","15分钟",TimeUnit.MINUTES,15L),
    HALF_HOURLY("30","30分钟",TimeUnit.MINUTES,30L),
    HOURLY("60","1小时",TimeUnit.HOURS,1L),
    TWO_HOURLY("120","2小时",TimeUnit.HOURS,2L),
    FOUR_HOURLY("240","4小时",TimeUnit.HOURS,4L),
    SIX_HOURLY("360","6小时",TimeUnit.HOURS,6L),
    TWELVE_HOURLY("720","12小时",TimeUnit.HOURS,12L);
//    DAILY("D","1天",TimeUnit.DAYS,1L),
//    WEEKLY("W","1周",TimeUnit.DAYS,7L),
//    MONTHLY("M","1月",TimeUnit.DAYS,30L);
    String code;
    String name;
    TimeUnit timeUnit;
    Long timeUnitLimit;

    MarketIntervalKV(String code, String name, TimeUnit timeUnit, Long timeUnitLimit) {
        this.code = code;
        this.name = name;
        this.timeUnit = timeUnit;
        this.timeUnitLimit = timeUnitLimit;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static MarketIntervalKV getByCode(String code) {
        MarketIntervalKV[] values = MarketIntervalKV.values();
        for (MarketIntervalKV value : values) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new RuntimeException(MessageUtils.message("bybit.task.error.unsupported.Interval",code));
    }

    public static MarketInterval getSourceByCode(String code) {
        MarketInterval[] values = MarketInterval.values();
        for (MarketInterval value : values) {
            if (value.getIntervalId().equalsIgnoreCase(code)) {
                return value;
            }
        }
        throw new RuntimeException(MessageUtils.message("bybit.task.error.unsupported.Interval",code));
    }
}
