package com.bitstrat.mapper;

import com.bitstrat.domain.CoinsFinancialFlowRecord;
import com.bitstrat.domain.vo.CoinsFinancialFlowRecordVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 交易所资金流水记录Mapper接口
 *
 * @author Lion Li
 * @date 2025-06-02
 */
public interface CoinsFinancialFlowRecordMapper extends BaseMapperPlus<CoinsFinancialFlowRecord, CoinsFinancialFlowRecordVo> {
    List<CoinsFinancialFlowRecordVo> selectInExchangeRecordIds(List<String> exchangeRecordIds);
}
