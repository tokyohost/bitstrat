package com.bitstrat.strategy.positionIMpl;

import com.bitstrat.domain.bo.PositionVo;
import com.bitstrat.domain.vo.CoinsTaskVo;
import com.bitstrat.strategy.PositionStrategy;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/3 15:53
 * @Content
 */
@Component
public class NoStrategy implements PositionStrategy {
    @Override
    public String typeName() {
        return "NoStrategy";
    }

    @Override
    public String desc() {
        return "NoStrategyDesc";
    }

    @Override
    public Integer typeId() {
        return 2;
    }

    @Override
    public boolean check(CoinsTaskVo task, PositionVo positionVo) {
        return true;
    }
}
