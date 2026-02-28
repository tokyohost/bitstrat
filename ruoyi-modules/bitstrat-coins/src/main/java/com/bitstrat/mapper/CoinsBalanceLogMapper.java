package com.bitstrat.mapper;

import com.bitstrat.domain.CoinsBalanceLog;
import com.bitstrat.domain.vo.CoinsBalanceLogVo;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.math.BigDecimal;

/**
 * 账户余额变动日志Mapper接口
 *
 * @author Lion Li
 * @date 2025-11-20
 */
public interface CoinsBalanceLogMapper extends BaseMapperPlus<CoinsBalanceLog, CoinsBalanceLogVo> {

    void reduceBalanceByUserId(@Param("changeAmount") BigDecimal changeAmount, @Param("userId") Long userId);

    void addBalanceByUserId(@Param("changeAmount") BigDecimal changeAmount, @Param("userId") Long userId);
}
