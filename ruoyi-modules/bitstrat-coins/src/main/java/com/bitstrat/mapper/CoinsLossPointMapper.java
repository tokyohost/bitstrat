package com.bitstrat.mapper;

import com.bitstrat.domain.Account;
import com.bitstrat.domain.CoinsApi;
import com.bitstrat.domain.CoinsLossPoint;
import com.bitstrat.domain.vo.CoinsLossPointVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 滑点管理Mapper接口
 *
 * @author Lion Li
 * @date 2025-04-11
 */
public interface CoinsLossPointMapper extends BaseMapperPlus<CoinsLossPoint, CoinsLossPointVo> {

    List<CoinsApi> selectAccountByClientId(String clientId);
}
