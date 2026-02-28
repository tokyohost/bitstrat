package com.bitstrat.service;

import com.bitstrat.domain.bo.CoinsArbitrageWarningConfigBo;
import com.bitstrat.domain.vo.CoinsArbitrageWarningConfigVo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 用户配置套利警告Service接口
 *
 * @author Lion Li
 * @date 2025-05-04
 */
public interface ICoinsArbitrageWarningConfigService {

    /**
     * 查询用户配置套利警告
     *
     * @param id 主键
     * @return 用户配置套利警告
     */
    CoinsArbitrageWarningConfigVo queryById(Long id);

    /**
     * 分页查询用户配置套利警告列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户配置套利警告分页列表
     */
    TableDataInfo<CoinsArbitrageWarningConfigVo> queryPageList(CoinsArbitrageWarningConfigBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的用户配置套利警告列表
     *
     * @param bo 查询条件
     * @return 用户配置套利警告列表
     */
    List<CoinsArbitrageWarningConfigVo> queryList(CoinsArbitrageWarningConfigBo bo);

    /**
     * 新增用户配置套利警告
     *
     * @param bo 用户配置套利警告
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsArbitrageWarningConfigBo bo);

    /**
     * 修改用户配置套利警告
     *
     * @param bo 用户配置套利警告
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsArbitrageWarningConfigBo bo);

    /**
     * 校验并批量删除用户配置套利警告信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    CoinsArbitrageWarningConfigVo getByTaskId(Integer arbitrageType, Long taskId);
}
