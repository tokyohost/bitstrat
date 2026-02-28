package com.bitstrat.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bitstrat.constant.BatchOrderTaskStatus;
import org.apache.commons.collections4.CollectionUtils;
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
import com.bitstrat.domain.bo.CoinsBatchBo;
import com.bitstrat.domain.vo.CoinsBatchVo;
import com.bitstrat.domain.CoinsBatch;
import com.bitstrat.mapper.CoinsBatchMapper;
import com.bitstrat.service.ICoinsBatchService;

import java.math.BigDecimal;
import java.util.*;

/**
 * 分批订单任务Service业务层处理
 *
 * @author Lion Li
 * @date 2025-04-26
 */
@RequiredArgsConstructor
@Service
public class CoinsBatchServiceImpl implements ICoinsBatchService {

    private final CoinsBatchMapper baseMapper;

    /**
     * 查询分批订单任务
     *
     * @param id 主键
     * @return 分批订单任务
     */
    @Override
    public CoinsBatchVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询分批订单任务列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分批订单任务分页列表
     */
    @Override
    public TableDataInfo<CoinsBatchVo> queryPageList(CoinsBatchBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsBatch> lqw = buildQueryWrapper(bo);
        Page<CoinsBatchVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的分批订单任务列表
     *
     * @param bo 查询条件
     * @return 分批订单任务列表
     */
    @Override
    public List<CoinsBatchVo> queryList(CoinsBatchBo bo) {
        LambdaQueryWrapper<CoinsBatch> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsBatch> buildQueryWrapper(CoinsBatchBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsBatch> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsBatch::getId);
        lqw.eq(bo.getTaskId() != null, CoinsBatch::getTaskId, bo.getTaskId());
        lqw.eq(StringUtils.isNotBlank(bo.getBuyEx()), CoinsBatch::getBuyEx, bo.getBuyEx());
        lqw.eq(bo.getBuyTotal() != null, CoinsBatch::getBuyTotal, bo.getBuyTotal());
        lqw.eq(StringUtils.isNotBlank(bo.getSellEx()), CoinsBatch::getSellEx, bo.getSellEx());
        lqw.eq(bo.getSellTotal() != null, CoinsBatch::getSellTotal, bo.getSellTotal());
        lqw.eq(bo.getTotalSize() != null, CoinsBatch::getTotalSize, bo.getTotalSize());
        lqw.eq(bo.getBatchTotal() != null, CoinsBatch::getBatchTotal, bo.getBatchTotal());
        lqw.eq(bo.getDoneBatch() != null, CoinsBatch::getDoneBatch, bo.getDoneBatch());
        lqw.eq(bo.getDoneSize() != null, CoinsBatch::getDoneSize, bo.getDoneSize());
        lqw.eq(bo.getStatus() != null, CoinsBatch::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getMsg()), CoinsBatch::getMsg, bo.getMsg());
        lqw.eq(bo.getStartTime() != null, CoinsBatch::getStartTime, bo.getStartTime());
        lqw.eq(bo.getEndTime() != null, CoinsBatch::getEndTime, bo.getEndTime());

        lqw.eq(CoinsBatch::getUserId, LoginHelper.getUserId());
        return lqw;
    }

    /**
     * 新增分批订单任务
     *
     * @param bo 分批订单任务
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsBatchBo bo) {
        CoinsBatch add = MapstructUtils.convert(bo, CoinsBatch.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改分批订单任务
     *
     * @param bo 分批订单任务
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsBatchBo bo) {
        CoinsBatch update = MapstructUtils.convert(bo, CoinsBatch.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsBatch entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除分批订单任务信息
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
    public List<CoinsBatchVo> selectRunningTask() {
        LambdaQueryWrapper<CoinsBatch> queryWrapper = new LambdaQueryWrapper<CoinsBatch>()
                .eq(CoinsBatch::getStatus, BatchOrderTaskStatus.RUNNING);

        return baseMapper.selectVoList(queryWrapper);
    }

    @Override
    public void updateDoneSizeById(Long id, BigDecimal doneSize,BigDecimal doneBuySize, BigDecimal doneSellSize) {
        LambdaUpdateWrapper<CoinsBatch> update = new LambdaUpdateWrapper<>();
        update.eq(CoinsBatch::getId, id);
        update.set(CoinsBatch::getDoneSize, doneSize);
        update.set(CoinsBatch::getDoneBuySize, doneBuySize);
        update.set(CoinsBatch::getDoneSellSize, doneSellSize);
        baseMapper.update(update);
    }

    @Override
    public void increaseDoneBatch(Long batchId) {
        baseMapper.increaseDoneBatch(batchId);
    }

    @Override
    public void updateStatusById(Long id, long success,String msg) {
        LambdaUpdateWrapper<CoinsBatch> update = new LambdaUpdateWrapper<>();
        update.eq(CoinsBatch::getId, id);
        update.set(CoinsBatch::getStatus, success);
        if(StringUtils.isNotBlank(msg)){
            update.set(CoinsBatch::getMsg, msg);
        }
        if (success == BatchOrderTaskStatus.SUCCESS) {
            update.set(CoinsBatch::getEndTime,new Date());
        }
        baseMapper.update(update);
    }

    @Override
    public int stop(CoinsBatchBo bo) {
        LambdaUpdateWrapper<CoinsBatch> update = new LambdaUpdateWrapper<>();
        update.eq(CoinsBatch::getId, bo.getId());
        update.set(CoinsBatch::getStatus, BatchOrderTaskStatus.STOPED);

        return baseMapper.update(update);
    }

    @Override
    public List<CoinsBatchVo> queryByIds(Set<Long> batchIds) {
        if(CollectionUtils.isEmpty(batchIds)){
            return List.of();
        }
        LambdaUpdateWrapper<CoinsBatch> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.in(CoinsBatch::getId, batchIds);


        return baseMapper.selectVoList(queryWrapper);
    }
}
