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
import com.bitstrat.domain.bo.CoinsBatchOrderBo;
import com.bitstrat.domain.vo.CoinsBatchOrderVo;
import com.bitstrat.domain.CoinsBatchOrder;
import com.bitstrat.mapper.CoinsBatchOrderMapper;
import com.bitstrat.service.ICoinsBatchOrderService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 分批任务订单记录Service业务层处理
 *
 * @author Lion Li
 * @date 2025-04-26
 */
@RequiredArgsConstructor
@Service
public class CoinsBatchOrderServiceImpl implements ICoinsBatchOrderService {

    private final CoinsBatchOrderMapper baseMapper;

    /**
     * 查询分批任务订单记录
     *
     * @param id 主键
     * @return 分批任务订单记录
     */
    @Override
    public CoinsBatchOrderVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询分批任务订单记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分批任务订单记录分页列表
     */
    @Override
    public TableDataInfo<CoinsBatchOrderVo> queryPageList(CoinsBatchOrderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsBatchOrder> lqw = buildQueryWrapper(bo);
        Page<CoinsBatchOrderVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的分批任务订单记录列表
     *
     * @param bo 查询条件
     * @return 分批任务订单记录列表
     */
    @Override
    public List<CoinsBatchOrderVo> queryList(CoinsBatchOrderBo bo) {
        LambdaQueryWrapper<CoinsBatchOrder> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsBatchOrder> buildQueryWrapper(CoinsBatchOrderBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsBatchOrder> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsBatchOrder::getId);
        lqw.eq(bo.getBatchId() != null, CoinsBatchOrder::getBatchId, bo.getBatchId());
        lqw.eq(bo.getCurrBatch() != null, CoinsBatchOrder::getCurrBatch, bo.getCurrBatch());
        lqw.eq(bo.getOrderSize() != null, CoinsBatchOrder::getOrderSize, bo.getOrderSize());
        lqw.eq(bo.getStatus() != null, CoinsBatchOrder::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getMsg()), CoinsBatchOrder::getMsg, bo.getMsg());
        lqw.eq(bo.getStartTime() != null, CoinsBatchOrder::getStartTime, bo.getStartTime());
        lqw.eq(bo.getEndTime() != null, CoinsBatchOrder::getEndTime, bo.getEndTime());
        lqw.eq(bo.getUserId() != null, CoinsBatchOrder::getUserId, bo.getUserId());
        return lqw;
    }

    /**
     * 新增分批任务订单记录
     *
     * @param bo 分批任务订单记录
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsBatchOrderBo bo) {
        CoinsBatchOrder add = MapstructUtils.convert(bo, CoinsBatchOrder.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改分批任务订单记录
     *
     * @param bo 分批任务订单记录
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsBatchOrderBo bo) {
        CoinsBatchOrder update = MapstructUtils.convert(bo, CoinsBatchOrder.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsBatchOrder entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除分批任务订单记录信息
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
