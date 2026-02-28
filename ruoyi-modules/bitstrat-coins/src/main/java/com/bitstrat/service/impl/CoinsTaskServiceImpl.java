package com.bitstrat.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.bitstrat.domain.bo.CoinsTaskBo;
import com.bitstrat.domain.vo.CoinsTaskVo;
import com.bitstrat.domain.CoinsTask;
import com.bitstrat.mapper.CoinsTaskMapper;
import com.bitstrat.service.ICoinsTaskService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 任务管理Service业务层处理
 *
 * @author Lion Li
 * @date 2025-04-01
 */
@RequiredArgsConstructor
@Service
public class CoinsTaskServiceImpl implements ICoinsTaskService {

    private final CoinsTaskMapper baseMapper;

    /**
     * 查询任务管理
     *
     * @param id 主键
     * @return 任务管理
     */
    @Override
    public CoinsTaskVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询任务管理列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 任务管理分页列表
     */
    @Override
    public TableDataInfo<CoinsTaskVo> queryPageList(CoinsTaskBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsTask> lqw = buildQueryWrapper(bo);
        Page<CoinsTaskVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的任务管理列表
     *
     * @param bo 查询条件
     * @return 任务管理列表
     */
    @Override
    public List<CoinsTaskVo> queryList(CoinsTaskBo bo) {
        LambdaQueryWrapper<CoinsTask> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsTask> buildQueryWrapper(CoinsTaskBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsTask> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsTask::getId);
        lqw.like(StringUtils.isNotBlank(bo.getName()), CoinsTask::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getSymbol()), CoinsTask::getSymbol, bo.getSymbol());
        lqw.eq(bo.getBalance() != null, CoinsTask::getBalance, bo.getBalance());
        lqw.eq(bo.getSingleOrder() != null, CoinsTask::getSingleOrder, bo.getSingleOrder());
        lqw.eq(bo.getColdSec() != null, CoinsTask::getColdSec, bo.getColdSec());
        lqw.eq(bo.getTotalBalance() != null, CoinsTask::getTotalBalance, bo.getTotalBalance());
        lqw.eq(bo.getLastOrderTime() != null, CoinsTask::getLastOrderTime, bo.getLastOrderTime());
        lqw.eq(StringUtils.isNotBlank(bo.getTaskType()), CoinsTask::getTaskType, bo.getTaskType());
        lqw.eq(bo.getAiWorkflowId() != null, CoinsTask::getAiWorkflowId, bo.getAiWorkflowId());
        lqw.eq(bo.getRoleId() != null, CoinsTask::getRoleId, bo.getRoleId());
        lqw.eq(bo.getCreateUserId() != null, CoinsTask::getCreateUserId, bo.getCreateUserId());
        lqw.eq(bo.getStatus() != null, CoinsTask::getStatus, bo.getStatus());
        lqw.eq(bo.getCreateBy() != null, CoinsTask::getCreateBy, bo.getCreateBy());
        return lqw;
    }

    /**
     * 新增任务管理
     *
     * @param bo 任务管理
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsTaskBo bo) {
        CoinsTask add = MapstructUtils.convert(bo, CoinsTask.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改任务管理
     *
     * @param bo 任务管理
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsTaskBo bo) {
        CoinsTask update = MapstructUtils.convert(bo, CoinsTask.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsTask entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除任务管理信息
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
}
