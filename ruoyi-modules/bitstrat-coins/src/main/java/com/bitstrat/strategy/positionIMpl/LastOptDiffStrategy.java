package com.bitstrat.strategy.positionIMpl;

import com.bitstrat.domain.bo.PositionVo;
import com.bitstrat.domain.vo.CoinsTaskVo;
import com.bitstrat.strategy.PositionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/3 15:53
 * @Content
 */
@Slf4j
@Component
public class  LastOptDiffStrategy implements PositionStrategy {
    @Override
    public String typeName() {
        return "LastOptDiffStrategy";
    }

    @Override
    public String desc() {
        return "LastOptDiffStrategyDesc";
    }

    @Override
    public Integer typeId() {
        return 1;
    }

    @Override
    public boolean check(CoinsTaskVo task, PositionVo positionVo) {
        log.info("上次相同条件");
        if ("buy".equalsIgnoreCase(positionVo.getType())) {
            //买入判断
            if(StringUtils.isEmpty(task.getLastBuyRole())){
                //还没买过
                return true;
            }
            if (task.getLastBuyRole().equalsIgnoreCase(positionVo.getCurrRole())) {
                //跟上次买入相同，不买
                return false;
            }else{
                //不同，买
                return true;
            }

        }else if("sell".equalsIgnoreCase(positionVo.getType())) {
            //卖出判断
            if(StringUtils.isEmpty(task.getLastSellRole())){
                //还没卖过
                return true;
            }
            if (task.getLastSellRole().equalsIgnoreCase(positionVo.getCurrRole())) {
                //跟上次卖相同，不卖
                return false;
            }else{
                //不同，卖
                return true;
            }

        }


        return false;
    }
}
