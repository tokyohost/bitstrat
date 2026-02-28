package com.bitstrat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bitstrat.domain.CoinsAbTask;
import com.bitstrat.domain.bo.CoinsAbTaskBo;
import com.bitstrat.domain.vo.CoinsAbTaskVo;
import com.bitstrat.mapper.CoinsAbTaskMapper;
import com.bitstrat.service.ICoinsAbTaskService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 价差套利任务Service业务层处理
 *
 * @author Lion Li
 * @date 2025-06-08
 */
@RequiredArgsConstructor
@Service
public class CoinsAbTaskServiceImpl implements ICoinsAbTaskService {

    private final CoinsAbTaskMapper baseMapper;

    /**
     * 查询价差套利任务
     *
     * @param id 主键
     * @return 价差套利任务
     */
    @Override
    public CoinsAbTaskVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询价差套利任务列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 价差套利任务分页列表
     */
    @Override
    public TableDataInfo<CoinsAbTaskVo> queryPageList(CoinsAbTaskBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsAbTask> lqw = buildQueryWrapper(bo);
        Page<CoinsAbTaskVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的价差套利任务列表
     *
     * @param bo 查询条件
     * @return 价差套利任务列表
     */
    @Override
    public List<CoinsAbTaskVo> queryList(CoinsAbTaskBo bo) {
        LambdaQueryWrapper<CoinsAbTask> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsAbTask> buildQueryWrapper(CoinsAbTaskBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsAbTask> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsAbTask::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getTaskId()), CoinsAbTask::getTaskId, bo.getTaskId());
        lqw.eq(StringUtils.isNotBlank(bo.getBody()), CoinsAbTask::getBody, bo.getBody());
        lqw.eq(bo.getUserId() != null, CoinsAbTask::getUserId, bo.getUserId());
        return lqw;
    }

    /**
     * 新增价差套利任务
     *
     * @param bo 价差套利任务
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsAbTaskBo bo) {
        CoinsAbTask add = MapstructUtils.convert(bo, CoinsAbTask.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改价差套利任务
     *
     * @param bo 价差套利任务
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsAbTaskBo bo) {
        CoinsAbTask update = MapstructUtils.convert(bo, CoinsAbTask.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsAbTask entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除价差套利任务信息
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
    public CoinsAbTaskVo queryByTaskId(String taskId) {
        QueryWrapper<CoinsAbTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CoinsAbTask::getTaskId, taskId);

        return baseMapper.selectVoOne(queryWrapper);
    }
}
