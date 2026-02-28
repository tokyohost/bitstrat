package com.bitstrat.mapper;

import com.bitstrat.domain.CoinsFeed;
import com.bitstrat.domain.vo.CoinsFeedVo;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

/**
 * 策略广场Mapper接口
 *
 * @author Lion Li
 * @date 2025-12-12
 */
public interface CoinsFeedMapper extends BaseMapperPlus<CoinsFeed, CoinsFeedVo> {

    void updateLikeCountById(@Param("id") Long id, @Param("count") int count);

}
