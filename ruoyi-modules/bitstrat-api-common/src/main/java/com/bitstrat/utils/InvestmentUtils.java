package com.bitstrat.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class InvestmentUtils {

    /**
     * 计算年化收益率
     * @param profit 收益金额
     * @param initialInvestment 初始投资金额
     * @param days 持有天数
     * @return 年化收益率（百分比形式）
     */
    public static BigDecimal calculateAnnualizedReturn(BigDecimal profit, BigDecimal initialInvestment, int days) {
        if (profit == null || initialInvestment == null || initialInvestment.compareTo(BigDecimal.ZERO) <= 0 || days <= 0) {
            return BigDecimal.ZERO;
        }

        // 计算收益率：收益 / 初始投资
        BigDecimal returnRate = profit.divide(initialInvestment, 10, BigDecimal.ROUND_HALF_UP);

        // 持有天数与365的比例
        BigDecimal annualMultiplier = BigDecimal.valueOf(365).divide(BigDecimal.valueOf(days), 10, BigDecimal.ROUND_HALF_UP);

        // 计算年化收益率
        BigDecimal annualizedReturn = BigDecimal.ONE.add(returnRate).pow(annualMultiplier.intValue()).subtract(BigDecimal.ONE);

        // 将年化收益率转为百分比形式
        return annualizedReturn.multiply(BigDecimal.valueOf(100));
    }

    /**
     * 计算两个时间相差的天数，若差值小于 0，则返回 1 天
     * @param createTime 创建时间
     * @param updateTime 更新时间
     * @return 两个时间相差的天数，若小于 0，则为 1 天
     */
    public static long calculateDaysDifference(Date createTime, Date updateTime) {
        if (createTime == null || updateTime == null) {
            return 1;
        }

        // 将 Date 转换为 LocalDate
        LocalDate createLocalDate = createTime.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate updateLocalDate = updateTime.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        // 计算日期差值
        long daysBetween = ChronoUnit.DAYS.between(createLocalDate, updateLocalDate);

        // 如果差值小于 0，则返回 1 天
        return daysBetween < 0 ? 1 : daysBetween;
    }
}
