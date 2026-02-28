package com.bitstrat.service;

import com.bitstrat.domain.vo.CoinsRankReversedVo;
import com.bitstrat.domain.bo.CoinsRankReversedBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 山寨币排行(反向)Service接口
 *
 * @author Lion Li
 * @date 2025-04-06
 */
public interface ICoinsRankReversedService {

    /**
     * 查询山寨币排行(反向)
     *
     * @param id 主键
     * @return 山寨币排行(反向)
     */
    CoinsRankReversedVo queryById(Long id);

    /**
     * 分页查询山寨币排行(反向)列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 山寨币排行(反向)分页列表
     */
    TableDataInfo<CoinsRankReversedVo> queryPageList(CoinsRankReversedBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的山寨币排行(反向)列表
     *
     * @param bo 查询条件
     * @return 山寨币排行(反向)列表
     */
    List<CoinsRankReversedVo> queryList(CoinsRankReversedBo bo);

    /**
     * 新增山寨币排行(反向)
     *
     * @param bo 山寨币排行(反向)
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsRankReversedBo bo);

    /**
     * 修改山寨币排行(反向)
     *
     * @param bo 山寨币排行(反向)
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsRankReversedBo bo);

    /**
     * 校验并批量删除山寨币排行(反向)信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    CoinsRankReversedBo selectBySymbol(String symbol);
}
