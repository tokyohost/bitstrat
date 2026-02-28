package com.bitstrat.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/11/25 17:33
 * @Content
 */

public class TokenCalcUtils {

    /**
     * 使用 BigDecimal 计算 Token 价格
     *
     * @param tokens 本次 token 数量
     * @param price100M 每 100M tokens 的价格
     * @return 本次费用（保留 4 位小数）
     */
    public static BigDecimal calcPrice(BigDecimal tokens, BigDecimal price100M) {
        BigDecimal base = new BigDecimal("100000000"); // 100M
        BigDecimal cost = price100M.multiply(tokens).divide(base, 4, RoundingMode.HALF_UP);
        return cost;
    }

    /**
     * 计算 token 使用成本（统一换算为 USD）
     *
     * costUSD = pricePerMillion * tokensUsed / 1_000_000 * rate
     *
     * @param pricePerMillion  每 100 万 token 的价格（交易币种）
     * @param tokensUsed       使用的 token 数量
     * @param rate             交易币种 → USD 的固定汇率（USD=1，RMB≈0.14）
     * @return USD 成本（BigDecimal，已统一精度）
     */
    public static BigDecimal calculateCostPerTokens(
        BigDecimal tokensUsed,
        BigDecimal pricePerMillion,
        BigDecimal rate
    ) {
        Objects.requireNonNull(pricePerMillion, "pricePerMillion must not be null");
        Objects.requireNonNull(tokensUsed, "tokensUsed must not be null");
        Objects.requireNonNull(rate, "rate must not be null");

        if (pricePerMillion.signum() < 0) {
            throw new IllegalArgumentException("pricePerMillion must be non-negative");
        }
        if (tokensUsed.signum() < 0) {
            throw new IllegalArgumentException("tokensUsed must be non-negative");
        }
        if (rate.signum() <= 0) {
            throw new IllegalArgumentException("rate must be positive");
        }

        // 中间计算精度（防止精度损失）
        final int INTERMEDIATE_SCALE = 12;
        final RoundingMode ROUNDING = RoundingMode.HALF_UP;

        // 最终账务精度（建议 6 位，或 2 位按你系统标准）
        final int FINAL_SCALE = 6;

        BigDecimal million = BigDecimal.valueOf(1_000_000);

        // 原币种 cost
        BigDecimal rawCost = pricePerMillion
            .multiply(tokensUsed)
            .divide(million, INTERMEDIATE_SCALE, ROUNDING);

        // 换算成 USD（固定汇率）
        BigDecimal usdCost = rawCost
            .multiply(rate)
            .setScale(FINAL_SCALE, ROUNDING);

        return usdCost.stripTrailingZeros();
    }


}
