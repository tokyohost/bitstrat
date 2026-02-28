package com.bitstrat.service;

import com.bitstrat.domain.vo.ABCloseFrom;
import com.bitstrat.domain.vo.ABOrderFrom;
import com.bitstrat.domain.vo.AbTaskFrom;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/9 17:54
 * @Content
 */

public interface ExOrderService {

    void oncePlace2ExOrder(ABOrderFrom from);

    void oncePlace2CloseExOrder(ABCloseFrom from);
}
