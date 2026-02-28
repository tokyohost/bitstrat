package com.bitstrat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bitstrat.domain.CoinsRank;
import com.bitstrat.domain.bo.CoinsRankBo;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.bitstrat.domain.bo.CoinsRankReversedBo;
import com.bitstrat.domain.vo.CoinsRankReversedVo;
import com.bitstrat.domain.CoinsRankReversed;
import com.bitstrat.mapper.CoinsRankReversedMapper;
import com.bitstrat.service.ICoinsRankReversedService;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Objects;

/**
 * 山寨币排行(反向)Service业务层处理
 *
 * @author Lion Li
 * @date 2025-04-06
 */
@RequiredArgsConstructor
@Service
public class CoinsRankReversedServiceImpl implements ICoinsRankReversedService {

    private final CoinsRankReversedMapper baseMapper;

    /**
     * 查询山寨币排行(反向)
     *
     * @param id 主键
     * @return 山寨币排行(反向)
     */
    @Override
    public CoinsRankReversedVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询山寨币排行(反向)列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 山寨币排行(反向)分页列表
     */
    @Override
    public TableDataInfo<CoinsRankReversedVo> queryPageList(CoinsRankReversedBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsRankReversed> lqw = buildQueryWrapper(bo);
        Page<CoinsRankReversedVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的山寨币排行(反向)列表
     *
     * @param bo 查询条件
     * @return 山寨币排行(反向)列表
     */
    @Override
    public List<CoinsRankReversedVo> queryList(CoinsRankReversedBo bo) {
        LambdaQueryWrapper<CoinsRankReversed> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsRankReversed> buildQueryWrapper(CoinsRankReversedBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsRankReversed> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsRankReversed::getId);
        lqw.like(StringUtils.isNotBlank(bo.getSymbol()), CoinsRankReversed::getSymbol, bo.getSymbol());
        lqw.eq(bo.getScore() != null, CoinsRankReversed::getScore, bo.getScore());
        lqw.eq(StringUtils.isNotBlank(bo.getMarketPrice()), CoinsRankReversed::getMarketPrice, bo.getMarketPrice());
        lqw.eq(bo.getPercentage() != null, CoinsRankReversed::getPercentage, bo.getPercentage());
        lqw.eq(bo.getRank() != null, CoinsRankReversed::getRank, bo.getRank());
        lqw.ge(CoinsRankReversed::getScore,0);
        return lqw;
    }

    /**
     * 新增山寨币排行(反向)
     *
     * @param bo 山寨币排行(反向)
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsRankReversedBo bo) {
        CoinsRankReversed add = MapstructUtils.convert(bo, CoinsRankReversed.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改山寨币排行(反向)
     *
     * @param bo 山寨币排行(反向)
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsRankReversedBo bo) {
        CoinsRankReversed update = MapstructUtils.convert(bo, CoinsRankReversed.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsRankReversed entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除山寨币排行(反向)信息
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
    public CoinsRankReversedBo selectBySymbol(String symbol) {
        LambdaQueryWrapper<CoinsRankReversed> lambda = new QueryWrapper<CoinsRankReversed>().lambda();
        lambda.eq(CoinsRankReversed::getSymbol, symbol);
        CoinsRankReversed coinsRank = baseMapper.selectOne(lambda);
        if (Objects.nonNull(coinsRank)) {
            CoinsRankReversedBo coinsRankBo = new CoinsRankReversedBo();
            BeanUtils.copyProperties(coinsRank, coinsRankBo);
            return coinsRankBo;
        }else{
            return null;
        }
    }
}
