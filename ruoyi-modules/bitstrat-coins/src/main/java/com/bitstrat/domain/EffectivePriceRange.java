package com.bitstrat.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/20 19:10
 * @Content
 *
 * "effective_price_range": {
 *      "min": <numeric>,
 *      "max": <numeric>,
 *      "basis": "<简短说明逻辑>"
 *   },
 */
@Data
public class EffectivePriceRange {
    BigDecimal min;
    BigDecimal max;
    String basis;

    public boolean isValidateRange() {
        if (Objects.nonNull(min) && Objects.nonNull(max)) {
            return true;
        }else{
            return false;
        }
    }

}
