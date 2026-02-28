package com.bitstrat.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class CommonUtils {
    public static long getPreviousHourTimestamp(Date date) {
        // 将 Date 转为 ZonedDateTime
        ZonedDateTime zdt = date.toInstant().atZone(ZoneId.systemDefault());

        // 前一个小时整点
        ZonedDateTime lastHour = zdt
            .minusHours(1)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);

        return lastHour.toInstant().toEpochMilli();
    }
    public static long getPreviousMinuteTimestamp(int minusMinute,Date date) {
        // 将 Date 转为 ZonedDateTime
        ZonedDateTime zdt = date.toInstant().atZone(ZoneId.systemDefault());

        // 前一个小时整点
        ZonedDateTime lastHour = zdt
            .minusMinutes(minusMinute)
            .withSecond(0)
            .withNano(0);

        return lastHour.toInstant().toEpochMilli();
    }
}
