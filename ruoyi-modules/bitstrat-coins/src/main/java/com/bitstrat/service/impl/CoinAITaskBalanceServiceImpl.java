package com.bitstrat.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.bitstrat.domain.bo.CoinAITaskBalanceBo;
import com.bitstrat.domain.vo.CoinAITaskBalanceVo;
import com.bitstrat.domain.CoinAITaskBalance;
import com.bitstrat.mapper.CoinTestAiMapper;
import com.bitstrat.service.ICoinAITaskBalanceService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Objects;

/**
 * AI 测试趋势Service业务层处理
 *
 * @author Lion Li
 * @date 2025-10-29
 */
@RequiredArgsConstructor
@Service
public class CoinAITaskBalanceServiceImpl implements ICoinAITaskBalanceService {

    private final CoinTestAiMapper baseMapper;

    /**
     * 查询AI 测试趋势
     *
     * @param id 主键
     * @return AI 测试趋势
     */
    @Override
    public CoinAITaskBalanceVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询AI 测试趋势列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return AI 测试趋势分页列表
     */
    @Override
    public TableDataInfo<CoinAITaskBalanceVo> queryPageList(CoinAITaskBalanceBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinAITaskBalance> lqw = buildQueryWrapper(bo);
        Page<CoinAITaskBalanceVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的AI 测试趋势列表
     *
     * @param bo 查询条件
     * @return AI 测试趋势列表
     */
    @Override
    public List<CoinAITaskBalanceVo> queryList(CoinAITaskBalanceBo bo) {
        LambdaQueryWrapper<CoinAITaskBalance> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinAITaskBalance> buildQueryWrapper(CoinAITaskBalanceBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinAITaskBalance> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinAITaskBalance::getId);
        lqw.eq(bo.getEquity() != null, CoinAITaskBalance::getEquity, bo.getEquity());
        lqw.eq(bo.getFreeBalance() != null, CoinAITaskBalance::getFreeBalance, bo.getFreeBalance());
        lqw.eq(bo.getTime() != null, CoinAITaskBalance::getTime, bo.getTime());
        lqw.eq(bo.getTaskId() != null, CoinAITaskBalance::getTaskId, bo.getTaskId());
        lqw.eq(bo.getCreateBy() != null, CoinAITaskBalance::getCreateBy, bo.getCreateBy());
        if(Objects.nonNull(bo.getStartDate()) && Objects.nonNull(bo.getEndDate())){
            lqw.between(CoinAITaskBalance::getCreateTime, bo.getStartDate(), bo.getEndDate());
        }else{
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime localDateTime = now.minusHours(24);
            lqw.between(CoinAITaskBalance::getCreateTime, localDateTime, now);
        }
        return lqw;
    }

    /**
     * 新增AI 测试趋势
     *
     * @param bo AI 测试趋势
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinAITaskBalanceBo bo) {
        CoinAITaskBalance add = MapstructUtils.convert(bo, CoinAITaskBalance.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改AI 测试趋势
     *
     * @param bo AI 测试趋势
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinAITaskBalanceBo bo) {
        CoinAITaskBalance update = MapstructUtils.convert(bo, CoinAITaskBalance.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinAITaskBalance entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除AI 测试趋势信息
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
