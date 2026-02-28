package com.bitstrat.service;

import com.bitstrat.domain.vo.CoinsBatchVo;
import com.bitstrat.domain.bo.CoinsBatchBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 分批订单任务Service接口
 *
 * @author Lion Li
 * @date 2025-04-26
 */
public interface ICoinsBatchService {

    /**
     * 查询分批订单任务
     *
     * @param id 主键
     * @return 分批订单任务
     */
    CoinsBatchVo queryById(Long id);

    /**
     * 分页查询分批订单任务列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分批订单任务分页列表
     */
    TableDataInfo<CoinsBatchVo> queryPageList(CoinsBatchBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的分批订单任务列表
     *
     * @param bo 查询条件
     * @return 分批订单任务列表
     */
    List<CoinsBatchVo> queryList(CoinsBatchBo bo);

    /**
     * 新增分批订单任务
     *
     * @param bo 分批订单任务
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsBatchBo bo);

    /**
     * 修改分批订单任务
     *
     * @param bo 分批订单任务
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsBatchBo bo);

    /**
     * 校验并批量删除分批订单任务信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<CoinsBatchVo> selectRunningTask();

    void updateDoneSizeById(Long id, BigDecimal doneSize, BigDecimal doneBuySize, BigDecimal doneSellSize);

    void increaseDoneBatch(Long id);

    void updateStatusById(Long id, long success,String msg);

    int stop(CoinsBatchBo bo);

    List<CoinsBatchVo> queryByIds(Set<Long> batchIds);
}
