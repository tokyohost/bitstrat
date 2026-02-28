package com.bitstrat.service.impl;

import com.bitstrat.domain.CoinsFinancialFlowRecord;
import com.bitstrat.domain.bo.CoinsFinancialFlowRecordBo;
import com.bitstrat.domain.vo.CoinsFinancialFlowRecordVo;
import com.bitstrat.mapper.CoinsFinancialFlowRecordMapper;
import com.bitstrat.service.ICoinsFinancialFlowRecordService;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 交易所资金流水记录Service业务层处理
 *
 * @author Lion Li
 * @date 2025-06-02
 */
@RequiredArgsConstructor
@Service
public class CoinsFinancialFlowRecordServiceImpl implements ICoinsFinancialFlowRecordService {

    private final CoinsFinancialFlowRecordMapper baseMapper;

    /**
     * 查询交易所资金流水记录
     *
     * @param id 主键
     * @return 交易所资金流水记录
     */
    @Override
    public CoinsFinancialFlowRecordVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询交易所资金流水记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 交易所资金流水记录分页列表
     */
    @Override
    public TableDataInfo<CoinsFinancialFlowRecordVo> queryPageList(CoinsFinancialFlowRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsFinancialFlowRecord> lqw = buildQueryWrapper(bo);
        Page<CoinsFinancialFlowRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的交易所资金流水记录列表
     *
     * @param bo 查询条件
     * @return 交易所资金流水记录列表
     */
    @Override
    public List<CoinsFinancialFlowRecordVo> queryList(CoinsFinancialFlowRecordBo bo) {
        LambdaQueryWrapper<CoinsFinancialFlowRecord> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsFinancialFlowRecord> buildQueryWrapper(CoinsFinancialFlowRecordBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsFinancialFlowRecord> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsFinancialFlowRecord::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getExchangeRecordId()), CoinsFinancialFlowRecord::getExchangeRecordId, bo.getExchangeRecordId());
        lqw.eq(StringUtils.isNotBlank(bo.getExchange()), CoinsFinancialFlowRecord::getExchange, bo.getExchange());
        lqw.eq(StringUtils.isNotBlank(bo.getFlowType()), CoinsFinancialFlowRecord::getFlowType, bo.getFlowType());
        lqw.eq(StringUtils.isNotBlank(bo.getSymbol()), CoinsFinancialFlowRecord::getSymbol, bo.getSymbol());
        lqw.eq(bo.getTimestamp() != null, CoinsFinancialFlowRecord::getTimestamp, bo.getTimestamp());
        lqw.eq(bo.getAmount() != null, CoinsFinancialFlowRecord::getAmount, bo.getAmount());
        lqw.eq(StringUtils.isNotBlank(bo.getAsset()), CoinsFinancialFlowRecord::getAsset, bo.getAsset());
        return lqw;
    }

    /**
     * 新增交易所资金流水记录
     *
     * @param bo 交易所资金流水记录
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsFinancialFlowRecordBo bo) {
        CoinsFinancialFlowRecord add = MapstructUtils.convert(bo, CoinsFinancialFlowRecord.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 批量插入
     */
    @Override
    public Boolean insertBatch(List<CoinsFinancialFlowRecordBo> list) {
        List<CoinsFinancialFlowRecord> add = MapstructUtils.convert(list, CoinsFinancialFlowRecord.class);
        if (CollectionUtils.isNotEmpty(add)) {
            List<CoinsFinancialFlowRecordVo> coinsFinancialFlowRecordVos = baseMapper.selectInExchangeRecordIds(
                    add.stream().map(CoinsFinancialFlowRecord::getExchangeRecordId).toList());
            // 有这个exchangeRecordId就不再插入
            add.removeIf(item -> coinsFinancialFlowRecordVos.stream().anyMatch(vo
                    -> vo.getExchangeRecordId().equals(item.getExchangeRecordId())));
            if (CollectionUtils.isNotEmpty(add)) {
                return baseMapper.insertBatch(add);
            }
        }
        return false;
    }

    /**
     * 修改交易所资金流水记录
     *
     * @param bo 交易所资金流水记录
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsFinancialFlowRecordBo bo) {
        CoinsFinancialFlowRecord update = MapstructUtils.convert(bo, CoinsFinancialFlowRecord.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsFinancialFlowRecord entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除交易所资金流水记录信息
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
