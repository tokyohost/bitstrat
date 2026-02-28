package com.bitstrat.service;

import com.bitstrat.domain.CoinAiTaskRequest;
import com.bitstrat.domain.bo.CoinAiTaskRequestBo;
import com.bitstrat.domain.vo.CoinAiTaskRequestVo;
import jakarta.validation.constraints.NotNull;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * AI 用户请求提示词Service接口
 *
 * @author Lion Li
 * @date 2025-11-01
 */
public interface ICoinTestAiRequestService {

    /**
     * 查询AI 用户请求提示词
     *
     * @param id 主键
     * @return AI 用户请求提示词
     */
    CoinAiTaskRequestVo queryById(Long id);

    /**
     * 分页查询AI 用户请求提示词列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return AI 用户请求提示词分页列表
     */
    TableDataInfo<CoinAiTaskRequestVo> queryPageList(CoinAiTaskRequestBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的AI 用户请求提示词列表
     *
     * @param bo 查询条件
     * @return AI 用户请求提示词列表
     */
    List<CoinAiTaskRequestVo> queryList(CoinAiTaskRequestBo bo);

    /**
     * 新增AI 用户请求提示词
     *
     * @param bo AI 用户请求提示词
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinAiTaskRequestBo bo);

    /**
     * 修改AI 用户请求提示词
     *
     * @param bo AI 用户请求提示词
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinAiTaskRequestBo bo);

    /**
     * 校验并批量删除AI 用户请求提示词信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    CoinAiTaskRequest queryByRequestKey(@NotNull(message = "主键不能为空") String key);
}
