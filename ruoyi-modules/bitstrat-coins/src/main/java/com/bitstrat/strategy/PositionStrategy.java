package com.bitstrat.strategy;

import com.bitstrat.domain.bo.PositionVo;
import com.bitstrat.domain.vo.CoinsTaskVo;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/2 10:53
 * @Content
 */

public interface PositionStrategy {
    public String typeName();
    public String desc();
    public Integer typeId();

    /**
     * 检查是否继续加减仓
     * @param task
     */
    public boolean check(CoinsTaskVo task, PositionVo positionVo);


}
