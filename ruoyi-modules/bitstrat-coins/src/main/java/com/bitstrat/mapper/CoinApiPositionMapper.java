package com.bitstrat.mapper;

import com.bitstrat.domain.CoinApiPosition;
import com.bitstrat.domain.vo.CoinApiPositionVo;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.math.BigDecimal;
import java.util.List;

/**
 * API 历史仓位数据Mapper接口
 *
 * @author Lion Li
 * @date 2025-12-29
 */
public interface CoinApiPositionMapper extends BaseMapperPlus<CoinApiPosition, CoinApiPositionVo> {

    List<String> selectIdsByApiIdAndCurrentId(@Param("posIds") List<String> posIds, @Param("apiId") Long apiId);

    BigDecimal querySharpeRatioByApiIdAndStartTime(@Param("apiId") Long apiId, @Param("time") Long time);
}
