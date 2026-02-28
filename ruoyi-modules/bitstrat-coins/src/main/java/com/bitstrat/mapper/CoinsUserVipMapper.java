package com.bitstrat.mapper;

import com.bitstrat.domain.CoinsUserVip;
import com.bitstrat.domain.vo.CoinsUserVipInfoVo;
import com.bitstrat.domain.vo.CoinsUserVipVo;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 用户VIP 状态Mapper接口
 *
 * @author Lion Li
 * @date 2025-05-14
 */
public interface CoinsUserVipMapper extends BaseMapperPlus<CoinsUserVip, CoinsUserVipVo> {
    List<CoinsUserVipInfoVo> selectUserVipInfoList(Long userId);
}
