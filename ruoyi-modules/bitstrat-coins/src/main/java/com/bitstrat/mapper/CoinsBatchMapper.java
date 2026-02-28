package com.bitstrat.mapper;

import com.bitstrat.domain.CoinsBatch;
import com.bitstrat.domain.vo.CoinsBatchVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 分批订单任务Mapper接口
 *
 * @author Lion Li
 * @date 2025-04-26
 */
public interface CoinsBatchMapper extends BaseMapperPlus<CoinsBatch, CoinsBatchVo> {

    void increaseDoneBatch(Long batchId);
}
