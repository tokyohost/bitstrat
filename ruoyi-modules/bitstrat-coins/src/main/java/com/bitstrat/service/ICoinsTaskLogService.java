package com.bitstrat.service;

import com.bitstrat.domain.vo.CoinsTaskLogVo;
import com.bitstrat.domain.bo.CoinsTaskLogBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 任务买入卖出日志Service接口
 *
 * @author Lion Li
 * @date 2025-04-01
 */
public interface ICoinsTaskLogService {

    /**
     * 查询任务买入卖出日志
     *
     * @param id 主键
     * @return 任务买入卖出日志
     */
    CoinsTaskLogVo queryById(Long id);

    /**
     * 分页查询任务买入卖出日志列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 任务买入卖出日志分页列表
     */
    TableDataInfo<CoinsTaskLogVo> queryPageList(CoinsTaskLogBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的任务买入卖出日志列表
     *
     * @param bo 查询条件
     * @return 任务买入卖出日志列表
     */
    List<CoinsTaskLogVo> queryList(CoinsTaskLogBo bo);

    /**
     * 新增任务买入卖出日志
     *
     * @param bo 任务买入卖出日志
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsTaskLogBo bo);

    /**
     * 修改任务买入卖出日志
     *
     * @param bo 任务买入卖出日志
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsTaskLogBo bo);

    /**
     * 校验并批量删除任务买入卖出日志信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
