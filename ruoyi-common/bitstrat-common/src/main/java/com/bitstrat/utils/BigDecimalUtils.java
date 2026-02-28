package com.bitstrat.utils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/22 15:44
 * @Content
 */

@Slf4j
public class BigDecimalUtils {
    /**
     * 安全转换 BigDecimal 为字符串，避免 NPE。
     * @param value BigDecimal 值，允许为 null
     * @return 转换后的字符串，若为 null 则返回 null
     */
    public static String toPlainString(BigDecimal value) {
        return value != null ? value.toPlainString() : null;
    }

    public static Long toLong(BigDecimal value) {
        return value != null ? value.longValue() : null;
    }

    /**
     * 安全转换 BigDecimal 为字符串，支持自定义默认值。
     * @param value BigDecimal 值，允许为 null
     * @param defaultValue 若 value 为 null，则返回该默认值
     * @return 转换后的字符串
     */
    public static String toPlainStringOrDefault(BigDecimal value, String defaultValue) {
        return value != null ? value.toPlainString() : defaultValue;
    }

    // 判断 BigDecimal a 是否是 b 的倍数
    public static boolean isMultiple(BigDecimal a, BigDecimal b) {
        // 如果 b 为 0，则返回 false，因为不能除以零
        if (b.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }

        // a 除以 b，结果应该是整数
        BigDecimal remainder = a.remainder(b);

        // 如果余数为 0，表示 a 是 b 的倍数
        return remainder.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 调整价格，使其符合步长和精度要求
     * @param price 原始价格
     * @param pricePlace 价格的小数位数
     * @param priceEndStep 价格步长
     * @return 调整后的价格
     */
    public static BigDecimal adjustPrice(BigDecimal price, int pricePlace, int priceEndStep) {
        // 判断价格精度，四舍五入到指定的小数位数
        price = price.setScale(pricePlace, RoundingMode.HALF_UP);

        // 判断价格是否满足步长条件
        BigDecimal step = getDecimalValue(pricePlace).multiply(BigDecimal.valueOf(priceEndStep));
        BigDecimal remainder = price.remainder(step);  // 计算余数

        // 如果价格不是步长的倍数，调整为最近的满足条件的价格
        if (remainder.compareTo(BigDecimal.ZERO) != 0) {

            BigDecimal oldPrice = price;
            price = price.subtract(remainder).add(step);  // 调整为下一个符合步长的价格
            log.warn("价格不满足步长 {} 修改价格为：{}",oldPrice,price);
        }

        return price;
    }

    /**
     * 校验并修正下单价格，使其符合 Binance 的价格精度和 tickSize 要求
     *
     * @param inputPrice    用户输入的价格
     * @param priceDecimals 价格允许的小数位数（由 tickSize 精度决定）
     * @param tickSize      最小价格间隔（tickSize）
     * @return PriceCheckResult 包含是否合法和修正后的价格
     */
    public static BigDecimal adjustBinancePrice(BigDecimal inputPrice, int priceDecimals, BigDecimal tickSize) {
        // 统一精度
        BigDecimal roundedPrice = inputPrice.setScale(priceDecimals, RoundingMode.DOWN);

        // tickSize 整数倍判断
        BigDecimal[] division = roundedPrice.divideAndRemainder(tickSize);
        boolean isValid = division[1].compareTo(BigDecimal.ZERO) == 0;

        // 计算最近合法价格（向下取整）
        BigDecimal validPrice = tickSize.multiply(new BigDecimal(division[0].toBigInteger()))
            .setScale(priceDecimals, RoundingMode.DOWN);
        if (!isValid) {
            log.warn("币安 价格不满足步长 {} 修改价格为：{}",tickSize,validPrice);
        }
        return isValid ? roundedPrice:validPrice;
    }

    public static void main(String[] args) {
        BigDecimal tickSize = new BigDecimal("0.0001");
        BigDecimal price = new BigDecimal("0.0434153");
        BigDecimal roundedPrice = adjustBinancePrice(price, tickSize.scale(), tickSize);
        System.out.println(roundedPrice);
    }



    /**
     * 根据小数位数获取对应的最小值 BigDecimal
     * @param scale 小数位数
     * @return 对应的小数值
     */
    public static BigDecimal getDecimalValue(int scale) {
        // 创建 1 的 BigDecimal
        BigDecimal value = new BigDecimal("1");

        // 除以 10 的 scale 次方，即 1 / (10^scale)
        BigDecimal result = value.divide(new BigDecimal(Math.pow(10, scale)), scale, BigDecimal.ROUND_HALF_UP);

        return result;
    }

//    public static void main(String[] args) {
//        BigDecimal price = new BigDecimal("0.223");  // 测试价格
//        int pricePlace = 3;  // 小数位数限制
//        int priceEndStep = 5;  // 价格步长
//
//        // 调整价格，使其满足条件
//        BigDecimal adjustedPrice = adjustPrice(price, pricePlace, priceEndStep);
//        System.out.println("调整后的价格: " + adjustedPrice);
//    }

    public static BigDecimal formatPercent(BigDecimal value) {
        return value.multiply(BigDecimal.valueOf(100)).setScale(5, RoundingMode.HALF_UP);
    }

    public static boolean isFundingTimeGapWithin30Minutes(long buyTime, long sellTime) {
        // 如果时间戳小于当前时间的秒级上限，说明是秒单位，需要转为毫秒
        long now = System.currentTimeMillis();
        if (buyTime < 1e11) buyTime *= 1000; // 小于10^11，大概率是秒单位
        if (sellTime < 1e11) sellTime *= 1000;

        long gapMillis = Math.abs(buyTime - sellTime);
        return gapMillis <= 30 * 60 * 1000; // 是否在30分钟内
    }
}
