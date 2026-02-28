package com.bitstrat.ai.domain;

import com.bitstrat.ai.domain.vo.CompareWindowRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/28 18:04
 * @Content
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StartCompareContext extends CompareContext{
    CompareWindowRecord compareWindowRecord = new CompareWindowRecord();

}
