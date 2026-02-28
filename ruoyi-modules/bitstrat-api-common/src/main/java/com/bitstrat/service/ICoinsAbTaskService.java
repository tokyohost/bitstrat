package com.bitstrat.service;

import com.bitstrat.domain.CoinsAbTask;
import com.bitstrat.domain.bo.CoinsAbTaskBo;
import com.bitstrat.domain.vo.CoinsAbTaskVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * 价差套利任务Service接口
 *
 * @author Lion Li
 * @date 2025-06-08
 */
public interface ICoinsAbTaskService {

    /**
     * 查询价差套利任务
     *
     * @param id 主键
     * @return 价差套利任务
     */
    CoinsAbTaskVo queryById(Long id);

    /**
     * 分页查询价差套利任务列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 价差套利任务分页列表
     */
    TableDataInfo<CoinsAbTaskVo> queryPageList(CoinsAbTaskBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的价差套利任务列表
     *
     * @param bo 查询条件
     * @return 价差套利任务列表
     */
    List<CoinsAbTaskVo> queryList(CoinsAbTaskBo bo);

    /**
     * 新增价差套利任务
     *
     * @param bo 价差套利任务
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsAbTaskBo bo);

    /**
     * 修改价差套利任务
     *
     * @param bo 价差套利任务
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsAbTaskBo bo);

    /**
     * 校验并批量删除价差套利任务信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    CoinsAbTaskVo queryByTaskId(String taskId);
}
