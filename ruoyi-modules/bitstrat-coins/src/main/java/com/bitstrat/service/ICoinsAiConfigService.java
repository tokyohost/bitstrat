package com.bitstrat.service;

import com.bitstrat.domain.CoinsAiConfig;
import com.bitstrat.domain.vo.CoinsAiConfigVo;
import com.bitstrat.domain.bo.CoinsAiConfigBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * ai 流水线配置Service接口
 *
 * @author Lion Li
 * @date 2025-04-01
 */
public interface ICoinsAiConfigService {

    /**
     * 查询ai 流水线配置
     *
     * @param id 主键
     * @return ai 流水线配置
     */
    CoinsAiConfigVo queryById(Long id);

    /**
     * 分页查询ai 流水线配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return ai 流水线配置分页列表
     */
    TableDataInfo<CoinsAiConfigVo> queryPageList(CoinsAiConfigBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的ai 流水线配置列表
     *
     * @param bo 查询条件
     * @return ai 流水线配置列表
     */
    List<CoinsAiConfigVo> queryList(CoinsAiConfigBo bo);

    /**
     * 新增ai 流水线配置
     *
     * @param bo ai 流水线配置
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsAiConfigBo bo);

    /**
     * 修改ai 流水线配置
     *
     * @param bo ai 流水线配置
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsAiConfigBo bo);

    /**
     * 校验并批量删除ai 流水线配置信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<CoinsAiConfigVo> querySelectList(CoinsAiConfigBo bo);
}
