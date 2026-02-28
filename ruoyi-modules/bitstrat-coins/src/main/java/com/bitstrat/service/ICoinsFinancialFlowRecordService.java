package com.bitstrat.service;

import com.bitstrat.domain.bo.CoinsFinancialFlowRecordBo;
import com.bitstrat.domain.vo.CoinsFinancialFlowRecordVo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 交易所资金流水记录Service接口
 *
 * @author Lion Li
 * @date 2025-06-02
 */
public interface ICoinsFinancialFlowRecordService {

    /**
     * 查询交易所资金流水记录
     *
     * @param id 主键
     * @return 交易所资金流水记录
     */
    CoinsFinancialFlowRecordVo queryById(Long id);

    /**
     * 分页查询交易所资金流水记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 交易所资金流水记录分页列表
     */
    TableDataInfo<CoinsFinancialFlowRecordVo> queryPageList(CoinsFinancialFlowRecordBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的交易所资金流水记录列表
     *
     * @param bo 查询条件
     * @return 交易所资金流水记录列表
     */
    List<CoinsFinancialFlowRecordVo> queryList(CoinsFinancialFlowRecordBo bo);

    /**
     * 新增交易所资金流水记录
     *
     * @param bo 交易所资金流水记录
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsFinancialFlowRecordBo bo);
    Boolean insertBatch(List<CoinsFinancialFlowRecordBo> list);
    /**
     * 修改交易所资金流水记录
     *
     * @param bo 交易所资金流水记录
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsFinancialFlowRecordBo bo);

    /**
     * 校验并批量删除交易所资金流水记录信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
