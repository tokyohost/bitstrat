package com.bitstrat.service;

import com.bitstrat.domain.vo.CoinAITaskBalanceVo;
import com.bitstrat.domain.bo.CoinAITaskBalanceBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * AI 测试趋势Service接口
 *
 * @author Lion Li
 * @date 2025-10-29
 */
public interface ICoinAITaskBalanceService {

    /**
     * 查询AI 测试趋势
     *
     * @param id 主键
     * @return AI 测试趋势
     */
    CoinAITaskBalanceVo queryById(Long id);

    /**
     * 分页查询AI 测试趋势列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return AI 测试趋势分页列表
     */
    TableDataInfo<CoinAITaskBalanceVo> queryPageList(CoinAITaskBalanceBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的AI 测试趋势列表
     *
     * @param bo 查询条件
     * @return AI 测试趋势列表
     */
    List<CoinAITaskBalanceVo> queryList(CoinAITaskBalanceBo bo);

    /**
     * 新增AI 测试趋势
     *
     * @param bo AI 测试趋势
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinAITaskBalanceBo bo);

    /**
     * 修改AI 测试趋势
     *
     * @param bo AI 测试趋势
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinAITaskBalanceBo bo);

    /**
     * 校验并批量删除AI 测试趋势信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
