package com.bitstrat.service;

import com.bitstrat.domain.Account;
import com.bitstrat.domain.CoinsApi;
import com.bitstrat.domain.vo.CoinsLossPointVo;
import com.bitstrat.domain.bo.CoinsLossPointBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 滑点管理Service接口
 *
 * @author Lion Li
 * @date 2025-04-11
 */
public interface ICoinsLossPointService {

    /**
     * 查询滑点管理
     *
     * @param id 主键
     * @return 滑点管理
     */
    CoinsLossPointVo queryById(Long id);

    /**
     * 分页查询滑点管理列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 滑点管理分页列表
     */
    TableDataInfo<CoinsLossPointVo> queryPageList(CoinsLossPointBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的滑点管理列表
     *
     * @param bo 查询条件
     * @return 滑点管理列表
     */
    List<CoinsLossPointVo> queryList(CoinsLossPointBo bo);

    /**
     * 新增滑点管理
     *
     * @param bo 滑点管理
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsLossPointBo bo);

    /**
     * 修改滑点管理
     *
     * @param bo 滑点管理
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsLossPointBo bo);

    /**
     * 校验并批量删除滑点管理信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    void syncAll();
    void syncDeleteAll();

    List<CoinsApi> selectAccountByClientId(String clientId);
}
