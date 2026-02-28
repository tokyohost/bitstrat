package com.bitstrat.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bitstrat.constant.TaskShareStatus;
import com.bitstrat.domain.QueryProfitParam;
import com.bitstrat.domain.vo.CoinsFeedVo;
import com.bitstrat.domain.vo.TaskAnalysisByDay;
import com.bitstrat.domain.vo.TaskProfitByDay;
import com.bitstrat.service.ICoinsFeedService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Service;
import com.bitstrat.domain.bo.CoinsAiTaskBo;
import com.bitstrat.domain.vo.CoinsAiTaskVo;
import com.bitstrat.domain.CoinsAiTask;
import com.bitstrat.mapper.CoinsAiTaskMapper;
import com.bitstrat.service.ICoinsAiTaskService;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI任务Service业务层处理
 *
 * @author Lion Li
 * @date 2025-11-24
 */
@RequiredArgsConstructor
@Service
public class CoinsAiTaskServiceImpl implements ICoinsAiTaskService {

    private final CoinsAiTaskMapper baseMapper;

    private final ICoinsFeedService coinsFeedService;

    @Override
    public CoinsAiTaskBo queryBoById(Long id) {
        CoinsAiTask coinsAiTask = baseMapper.selectById(id);
        CoinsAiTaskBo taskBo = MapstructUtils.convert(coinsAiTask, CoinsAiTaskBo.class);
        return taskBo;
    }

    /**
     * 查询AI任务
     *
     * @param id 主键
     * @return AI任务
     */
    @Override
    public CoinsAiTaskVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询AI任务列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return AI任务分页列表
     */
    @Override
    public TableDataInfo<CoinsAiTaskVo> queryPageList(CoinsAiTaskBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsAiTask> lqw = buildQueryWrapper(bo);
        Page<CoinsAiTaskVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        List<CoinsAiTaskVo> records = result.getRecords();
        //查分享状态
        List<Long> taskIds = records.stream().map(CoinsAiTaskVo::getId).collect(Collectors.toList());
        List<CoinsFeedVo> coinsFeedVos = coinsFeedService.queryListByTaskIds(taskIds);
        Map<Long, Long> publishTaskIds = coinsFeedVos.stream().collect(Collectors.toMap(CoinsFeedVo::getStrategyId, CoinsFeedVo::getId, (a, b) -> a));
        for (CoinsAiTaskVo record : records) {
            if (publishTaskIds.containsKey(record.getId())) {
                record.setShareStatus(TaskShareStatus.SHARED.getStatus());
                record.setShareId(publishTaskIds.get(record.getId()));
            }else{
                record.setShareStatus(TaskShareStatus.NOT_SHARE.getStatus());
            }
        }
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的AI任务列表
     *
     * @param bo 查询条件
     * @return AI任务列表
     */
    @Override
    public List<CoinsAiTaskVo> queryList(CoinsAiTaskBo bo) {
        LambdaQueryWrapper<CoinsAiTask> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsAiTask> buildQueryWrapper(CoinsAiTaskBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsAiTask> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsAiTask::getId);
        lqw.like(StringUtils.isNotBlank(bo.getName()), CoinsAiTask::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getSymbols()), CoinsAiTask::getSymbols, bo.getSymbols());
        lqw.eq(bo.getStartBalance() != null, CoinsAiTask::getStartBalance, bo.getStartBalance());
        lqw.eq(bo.getTotalBalance() != null, CoinsAiTask::getTotalBalance, bo.getTotalBalance());
        lqw.eq(bo.getAiWorkflowId() != null, CoinsAiTask::getAiWorkflowId, bo.getAiWorkflowId());
        lqw.eq(StringUtils.isNotBlank(bo.getSystemPrompt()), CoinsAiTask::getSystemPrompt, bo.getSystemPrompt());
        lqw.eq(StringUtils.isNotBlank(bo.getUserPrompt()), CoinsAiTask::getUserPrompt, bo.getUserPrompt());
        lqw.eq(bo.getCreateUserId() != null, CoinsAiTask::getCreateUserId, bo.getCreateUserId());
        lqw.eq(bo.getStatus() != null, CoinsAiTask::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getInterval()), CoinsAiTask::getInterval, bo.getInterval());
        return lqw;
    }

    /**
     * 新增AI任务
     *
     * @param bo AI任务
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsAiTaskBo bo) {
        CoinsAiTask add = MapstructUtils.convert(bo, CoinsAiTask.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改AI任务
     *
     * @param bo AI任务
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsAiTaskBo bo) {
        CoinsAiTask update = MapstructUtils.convert(bo, CoinsAiTask.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsAiTask entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除AI任务信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public void stopTask(CoinsAiTask coinsAiTask) {
        LambdaUpdateWrapper<CoinsAiTask> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CoinsAiTask::getId, coinsAiTask.getId());
        updateWrapper.set(CoinsAiTask::getStatus, 3);
        this.baseMapper.update(updateWrapper);
    }

    @Override
    public void checkExistsTask(Long apiId) {
        LambdaQueryWrapper<CoinsAiTask> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(CoinsAiTask::getApiId, apiId);
        updateWrapper.eq(CoinsAiTask::getCreateUserId, LoginHelper.getUserId());
        Long l = this.baseMapper.selectCount(updateWrapper);
        if (l > 0) {
            throw new RuntimeException("您已创建相同API 的任务，请勿重复创建");
        }


    }

    @Override
    public void checkMaxTask(Long userId, int max) {
        LambdaQueryWrapper<CoinsAiTask> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(CoinsAiTask::getCreateUserId, LoginHelper.getUserId());
        Long l = this.baseMapper.selectCount(updateWrapper);
        if (l > max) {
            throw new RuntimeException("内测期间您最多只能创建"+max+"个任务！");
        }

    }

    @Override
    public List<CoinsAiTaskVo> queryListByIds(List<Long> ids) {
        LambdaQueryWrapper<CoinsAiTask> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.in(CoinsAiTask::getId, ids);

        return this.baseMapper.selectVoList(updateWrapper);
    }

    @Override
    public List<TaskProfitByDay> queryTaskProfit(QueryProfitParam queryProfitParam) {
        return baseMapper.queryTaskProfit(queryProfitParam);
    }

    @Override
    public List<TaskAnalysisByDay> queryTaskDayProfit(QueryProfitParam queryProfitParam) {

        return baseMapper.queryTaskDayProfit(queryProfitParam);
    }
}
