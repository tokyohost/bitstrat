package com.bitstrat.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bitstrat.domain.CoinsArbitrageWarningConfig;
import com.bitstrat.domain.bo.CoinsArbitrageWarningConfigBo;
import com.bitstrat.domain.vo.CoinsArbitrageWarningConfigVo;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 用户配置套利警告Mapper接口
 *
 * @author Lion Li
 * @date 2025-05-04
 */
public interface CoinsArbitrageWarningConfigMapper extends BaseMapperPlus<CoinsArbitrageWarningConfig, CoinsArbitrageWarningConfigVo> {
    Page<CoinsArbitrageWarningConfigVo> selectCoinsArbitrageWarningConfigList(@Param("warningConfig") CoinsArbitrageWarningConfigBo bo, IPage<CoinsArbitrageWarningConfigVo> page);
}
