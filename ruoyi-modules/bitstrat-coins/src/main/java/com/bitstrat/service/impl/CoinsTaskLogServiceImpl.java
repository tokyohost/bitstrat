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
import com.bitstrat.domain.bo.CoinsTaskLogBo;
import com.bitstrat.domain.vo.CoinsTaskLogVo;
import com.bitstrat.domain.CoinsTaskLog;
import com.bitstrat.mapper.CoinsTaskLogMapper;
import com.bitstrat.service.ICoinsTaskLogService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 任务买入卖出日志Service业务层处理
 *
 * @author Lion Li
 * @date 2025-04-01
 */
@RequiredArgsConstructor
@Service
public class CoinsTaskLogServiceImpl implements ICoinsTaskLogService {

    private final CoinsTaskLogMapper baseMapper;

    /**
     * 查询任务买入卖出日志
     *
     * @param id 主键
     * @return 任务买入卖出日志
     */
    @Override
    public CoinsTaskLogVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询任务买入卖出日志列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 任务买入卖出日志分页列表
     */
    @Override
    public TableDataInfo<CoinsTaskLogVo> queryPageList(CoinsTaskLogBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsTaskLog> lqw = buildQueryWrapper(bo);
        Page<CoinsTaskLogVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的任务买入卖出日志列表
     *
     * @param bo 查询条件
     * @return 任务买入卖出日志列表
     */
    @Override
    public List<CoinsTaskLogVo> queryList(CoinsTaskLogBo bo) {
        LambdaQueryWrapper<CoinsTaskLog> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsTaskLog> buildQueryWrapper(CoinsTaskLogBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsTaskLog> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsTaskLog::getId);
        lqw.eq(bo.getTaskId() != null, CoinsTaskLog::getTaskId, bo.getTaskId());
        lqw.eq(bo.getPrice() != null, CoinsTaskLog::getPrice, bo.getPrice());
        lqw.eq(bo.getCount() != null, CoinsTaskLog::getCount, bo.getCount());
        lqw.eq(bo.getType() != null, CoinsTaskLog::getType, bo.getType());
        lqw.eq(bo.getTotal() != null, CoinsTaskLog::getTotal, bo.getTotal());
        return lqw;
    }

    /**
     * 新增任务买入卖出日志
     *
     * @param bo 任务买入卖出日志
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsTaskLogBo bo) {
        CoinsTaskLog add = MapstructUtils.convert(bo, CoinsTaskLog.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改任务买入卖出日志
     *
     * @param bo 任务买入卖出日志
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsTaskLogBo bo) {
        CoinsTaskLog update = MapstructUtils.convert(bo, CoinsTaskLog.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsTaskLog entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除任务买入卖出日志信息
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
