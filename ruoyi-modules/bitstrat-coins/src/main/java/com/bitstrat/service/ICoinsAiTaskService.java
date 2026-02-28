package com.bitstrat.service;

import com.bitstrat.domain.CoinsAiTask;
import com.bitstrat.domain.QueryProfitParam;
import com.bitstrat.domain.vo.CoinsAiTaskVo;
import com.bitstrat.domain.bo.CoinsAiTaskBo;
import com.bitstrat.domain.vo.TaskAnalysisByDay;
import com.bitstrat.domain.vo.TaskProfitByDay;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * AI任务Service接口
 *
 * @author Lion Li
 * @date 2025-11-24
 */
public interface ICoinsAiTaskService {

    /**
     * 查询AI任务
     *
     * @param id 主键
     * @return AI任务
     */
    CoinsAiTaskVo queryById(Long id);
    CoinsAiTaskBo queryBoById(Long id);

    /**
     * 分页查询AI任务列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return AI任务分页列表
     */
    TableDataInfo<CoinsAiTaskVo> queryPageList(CoinsAiTaskBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的AI任务列表
     *
     * @param bo 查询条件
     * @return AI任务列表
     */
    List<CoinsAiTaskVo> queryList(CoinsAiTaskBo bo);

    /**
     * 新增AI任务
     *
     * @param bo AI任务
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsAiTaskBo bo);

    /**
     * 修改AI任务
     *
     * @param bo AI任务
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsAiTaskBo bo);

    /**
     * 校验并批量删除AI任务信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    void stopTask(CoinsAiTask coinsAiTask);

    void checkExistsTask(Long apiId);

    void checkMaxTask(Long userId, int max);

    List<CoinsAiTaskVo> queryListByIds(List<Long> ids);

    List<TaskProfitByDay> queryTaskProfit(QueryProfitParam queryProfitParam);

    List<TaskAnalysisByDay> queryTaskDayProfit(QueryProfitParam queryProfitParam);
}
