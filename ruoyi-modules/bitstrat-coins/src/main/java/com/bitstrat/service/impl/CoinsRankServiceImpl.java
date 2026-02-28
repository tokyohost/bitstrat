package com.bitstrat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import com.bitstrat.domain.bo.CoinsRankBo;
import com.bitstrat.domain.vo.CoinsRankVo;
import com.bitstrat.domain.CoinsRank;
import com.bitstrat.mapper.CoinsRankMapper;
import com.bitstrat.service.ICoinsRankService;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Objects;

/**
 * 山寨币排行Service业务层处理
 *
 * @author Lion Li
 * @date 2025-04-05
 */
@RequiredArgsConstructor
@Service
public class CoinsRankServiceImpl implements ICoinsRankService {

    private final CoinsRankMapper baseMapper;

    /**
     * 查询山寨币排行
     *
     * @param id 主键
     * @return 山寨币排行
     */
    @Override
    public CoinsRankVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询山寨币排行列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 山寨币排行分页列表
     */
    @Override
    public TableDataInfo<CoinsRankVo> queryPageList(CoinsRankBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsRank> lqw = buildQueryWrapper(bo);
        Page<CoinsRankVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的山寨币排行列表
     *
     * @param bo 查询条件
     * @return 山寨币排行列表
     */
    @Override
    public List<CoinsRankVo> queryList(CoinsRankBo bo) {
        LambdaQueryWrapper<CoinsRank> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsRank> buildQueryWrapper(CoinsRankBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsRank> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsRank::getId);
        lqw.like(StringUtils.isNotBlank(bo.getSymbol()), CoinsRank::getSymbol, bo.getSymbol());
        lqw.eq(bo.getScore() != null, CoinsRank::getScore, bo.getScore());
        lqw.eq(StringUtils.isNotBlank(bo.getMarketPrice()), CoinsRank::getMarketPrice, bo.getMarketPrice());
        lqw.eq(bo.getPercentage() != null, CoinsRank::getPercentage, bo.getPercentage());
        lqw.ge(CoinsRank::getScore,0);
        return lqw;
    }

    /**
     * 新增山寨币排行
     *
     * @param bo 山寨币排行
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsRankBo bo) {
        CoinsRank add = MapstructUtils.convert(bo, CoinsRank.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改山寨币排行
     *
     * @param bo 山寨币排行
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsRankBo bo) {
        CoinsRank update = MapstructUtils.convert(bo, CoinsRank.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsRank entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除山寨币排行信息
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
    public CoinsRankBo selectBySymbol(String symbol) {
        LambdaQueryWrapper<CoinsRank> lambda = new QueryWrapper<CoinsRank>().lambda();
        lambda.eq(CoinsRank::getSymbol, symbol);
        CoinsRank coinsRank = baseMapper.selectOne(lambda);
        if (Objects.nonNull(coinsRank)) {
            CoinsRankBo coinsRankBo = new CoinsRankBo();
            BeanUtils.copyProperties(coinsRank, coinsRankBo);
            return coinsRankBo;
        }else{
            return null;
        }

    }
}
