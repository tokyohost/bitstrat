package com.bitstrat.service;

import com.bitstrat.domain.vo.CoinsTaskVo;
import com.bitstrat.domain.bo.CoinsTaskBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 任务管理Service接口
 *
 * @author Lion Li
 * @date 2025-04-01
 */
public interface ICoinsTaskService {

    /**
     * 查询任务管理
     *
     * @param id 主键
     * @return 任务管理
     */
    CoinsTaskVo queryById(Long id);

    /**
     * 分页查询任务管理列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 任务管理分页列表
     */
    TableDataInfo<CoinsTaskVo> queryPageList(CoinsTaskBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的任务管理列表
     *
     * @param bo 查询条件
     * @return 任务管理列表
     */
    List<CoinsTaskVo> queryList(CoinsTaskBo bo);

    /**
     * 新增任务管理
     *
     * @param bo 任务管理
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsTaskBo bo);

    /**
     * 修改任务管理
     *
     * @param bo 任务管理
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsTaskBo bo);

    /**
     * 校验并批量删除任务管理信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
