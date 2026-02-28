package com.bitstrat.mapper;

import com.bitstrat.domain.CoinsCrossExchangeArbitrageTask;
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;
import java.util.Map;

/**
 * 跨交易所套利任务Mapper接口
 *
 * @author Lion Li
 * @date 2025-04-19
 */
public interface CoinsCrossExchangeArbitrageTaskMapper extends BaseMapperPlus<CoinsCrossExchangeArbitrageTask, CoinsCrossExchangeArbitrageTaskVo> {

    List<CoinsCrossExchangeArbitrageTaskVo> selectVoListWithWarning(Map<String, Object> params);
}
