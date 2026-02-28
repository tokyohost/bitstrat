package com.bitstrat.service;

import com.bitstrat.domain.vo.CoinsRankVo;
import com.bitstrat.domain.bo.CoinsRankBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 山寨币排行Service接口
 *
 * @author Lion Li
 * @date 2025-04-05
 */
public interface ICoinsRankService {

    /**
     * 查询山寨币排行
     *
     * @param id 主键
     * @return 山寨币排行
     */
    CoinsRankVo queryById(Long id);

    /**
     * 分页查询山寨币排行列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 山寨币排行分页列表
     */
    TableDataInfo<CoinsRankVo> queryPageList(CoinsRankBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的山寨币排行列表
     *
     * @param bo 查询条件
     * @return 山寨币排行列表
     */
    List<CoinsRankVo> queryList(CoinsRankBo bo);

    /**
     * 新增山寨币排行
     *
     * @param bo 山寨币排行
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsRankBo bo);

    /**
     * 修改山寨币排行
     *
     * @param bo 山寨币排行
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsRankBo bo);

    /**
     * 校验并批量删除山寨币排行信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    CoinsRankBo selectBySymbol(String symbol);
}
