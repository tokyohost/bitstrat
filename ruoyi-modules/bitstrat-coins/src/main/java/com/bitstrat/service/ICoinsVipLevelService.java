package com.bitstrat.service;

import com.bitstrat.domain.bo.CoinsVipLevelBo;
import com.bitstrat.domain.vo.CoinsVipLevelVo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * VIP 权限Service接口
 *
 * @author Lion Li
 * @date 2025-05-14
 */
public interface ICoinsVipLevelService {

    /**
     * 查询VIP 权限
     *
     * @param id 主键
     * @return VIP 权限
     */
    CoinsVipLevelVo queryById(Long id);

    /**
     * 分页查询VIP 权限列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return VIP 权限分页列表
     */
    TableDataInfo<CoinsVipLevelVo> queryPageList(CoinsVipLevelBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的VIP 权限列表
     *
     * @param bo 查询条件
     * @return VIP 权限列表
     */
    List<CoinsVipLevelVo> queryList(CoinsVipLevelBo bo);

    /**
     * 新增VIP 权限
     *
     * @param bo VIP 权限
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsVipLevelBo bo);

    /**
     * 修改VIP 权限
     *
     * @param bo VIP 权限
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsVipLevelBo bo);

    /**
     * 校验并批量删除VIP 权限信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<CoinsVipLevelVo> getAvailableVipLevelList(CoinsVipLevelBo bo);
}
