package com.bitstrat.service;

import com.bitstrat.domain.vo.CoinsRankLogVo;
import com.bitstrat.domain.bo.CoinsRankLogBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 山寨币排行日志Service接口
 *
 * @author Lion Li
 * @date 2025-04-05
 */
public interface ICoinsRankLogService {

    /**
     * 查询山寨币排行日志
     *
     * @param id 主键
     * @return 山寨币排行日志
     */
    CoinsRankLogVo queryById(Long id);

    /**
     * 分页查询山寨币排行日志列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 山寨币排行日志分页列表
     */
    TableDataInfo<CoinsRankLogVo> queryPageList(CoinsRankLogBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的山寨币排行日志列表
     *
     * @param bo 查询条件
     * @return 山寨币排行日志列表
     */
    List<CoinsRankLogVo> queryList(CoinsRankLogBo bo);

    /**
     * 新增山寨币排行日志
     *
     * @param bo 山寨币排行日志
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsRankLogBo bo);

    /**
     * 修改山寨币排行日志
     *
     * @param bo 山寨币排行日志
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsRankLogBo bo);

    /**
     * 校验并批量删除山寨币排行日志信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
