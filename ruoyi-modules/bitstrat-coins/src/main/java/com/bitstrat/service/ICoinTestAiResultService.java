package com.bitstrat.service;

import com.bitstrat.domain.vo.CoinTestAiResultVo;
import com.bitstrat.domain.bo.CoinTestAiResultBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * AI 操作日志Service接口
 *
 * @author Lion Li
 * @date 2025-10-30
 */
public interface ICoinTestAiResultService {

    /**
     * 查询AI 操作日志
     *
     * @param id 主键
     * @return AI 操作日志
     */
    CoinTestAiResultVo queryById(Long id);

    /**
     * 分页查询AI 操作日志列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return AI 操作日志分页列表
     */
    TableDataInfo<CoinTestAiResultVo> queryPageList(CoinTestAiResultBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的AI 操作日志列表
     *
     * @param bo 查询条件
     * @return AI 操作日志列表
     */
    List<CoinTestAiResultVo> queryList(CoinTestAiResultBo bo);

    /**
     * 新增AI 操作日志
     *
     * @param bo AI 操作日志
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinTestAiResultBo bo);

    /**
     * 修改AI 操作日志
     *
     * @param bo AI 操作日志
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinTestAiResultBo bo);

    /**
     * 校验并批量删除AI 操作日志信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
