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
import com.bitstrat.domain.bo.CoinTestAiResultBo;
import com.bitstrat.domain.vo.CoinTestAiResultVo;
import com.bitstrat.domain.CoinTestAiResult;
import com.bitstrat.mapper.CoinTestAiResultMapper;
import com.bitstrat.service.ICoinTestAiResultService;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Objects;

/**
 * AI 操作日志Service业务层处理
 *
 * @author Lion Li
 * @date 2025-10-30
 */
@RequiredArgsConstructor
@Service
public class CoinTestAiResultServiceImpl implements ICoinTestAiResultService {

    private final CoinTestAiResultMapper baseMapper;

    /**
     * 查询AI 操作日志
     *
     * @param id 主键
     * @return AI 操作日志
     */
    @Override
    public CoinTestAiResultVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询AI 操作日志列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return AI 操作日志分页列表
     */
    @Override
    public TableDataInfo<CoinTestAiResultVo> queryPageList(CoinTestAiResultBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinTestAiResult> lqw = buildQueryWrapper(bo);
        Page<CoinTestAiResultVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的AI 操作日志列表
     *
     * @param bo 查询条件
     * @return AI 操作日志列表
     */
    @Override
    public List<CoinTestAiResultVo> queryList(CoinTestAiResultBo bo) {
        LambdaQueryWrapper<CoinTestAiResult> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinTestAiResult> buildQueryWrapper(CoinTestAiResultBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinTestAiResult> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinTestAiResult::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getAction()), CoinTestAiResult::getAction, bo.getAction());
        lqw.eq(bo.getLeverage() != null, CoinTestAiResult::getLeverage, bo.getLeverage());
        lqw.eq(StringUtils.isNotBlank(bo.getSize()), CoinTestAiResult::getSize, bo.getSize());
        lqw.eq(StringUtils.isNotBlank(bo.getSymbol()), CoinTestAiResult::getSymbol, bo.getSymbol());
        lqw.eq(StringUtils.isNotBlank(bo.getTakeProfit()), CoinTestAiResult::getTakeProfit, bo.getTakeProfit());
        lqw.eq(StringUtils.isNotBlank(bo.getStopLoss()), CoinTestAiResult::getStopLoss, bo.getStopLoss());
        lqw.eq(StringUtils.isNotBlank(bo.getReasoningEn()), CoinTestAiResult::getReasoningEn, bo.getReasoningEn());
        lqw.eq(StringUtils.isNotBlank(bo.getReasoningZh()), CoinTestAiResult::getReasoningZh, bo.getReasoningZh());
        lqw.eq(Objects.nonNull(bo.getTaskId()), CoinTestAiResult::getTaskId, bo.getTaskId());
        lqw.eq(Objects.nonNull(bo.getCreateBy()), CoinTestAiResult::getCreateBy, bo.getCreateBy());
        return lqw;
    }

    /**
     * 新增AI 操作日志
     *
     * @param bo AI 操作日志
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinTestAiResultBo bo) {
        CoinTestAiResult add = MapstructUtils.convert(bo, CoinTestAiResult.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改AI 操作日志
     *
     * @param bo AI 操作日志
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinTestAiResultBo bo) {
        CoinTestAiResult update = MapstructUtils.convert(bo, CoinTestAiResult.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinTestAiResult entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除AI 操作日志信息
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
