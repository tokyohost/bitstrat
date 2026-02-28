package com.bitstrat.ai.domain;

import com.bitstrat.ai.constant.BusinessType;
import com.bitstrat.domain.Account;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/28 17:31
 * @Content 对比内容
 */

@Data
public class CompareItem {
    private String exchange;
    private String symbol;
    /**
     * see {@link BusinessType#COMPARE_TYPE_SWAP}
     * see {@link BusinessType#COMPARE_TYPE_SPOT}
     */
    private String type;

    private Account account;
}
