package com.bitstrat.mapper;

import com.bitstrat.domain.CoinsApi;
import com.bitstrat.domain.vo.CoinsApiVo;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.math.BigDecimal;

/**
 * 交易所APIMapper接口
 *
 * @author Lion Li
 * @date 2025-04-14
 */
public interface CoinsApiMapper extends BaseMapperPlus<CoinsApi, CoinsApiVo> {

    void updateBalanceAndFreeById(@Param("accountId") Long accountId, @Param("balance") BigDecimal balance, @Param("freeBalance") BigDecimal freeBalance);
}
