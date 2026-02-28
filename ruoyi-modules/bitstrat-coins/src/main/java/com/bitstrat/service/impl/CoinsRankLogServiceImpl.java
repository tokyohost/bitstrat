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
import com.bitstrat.domain.bo.CoinsRankLogBo;
import com.bitstrat.domain.vo.CoinsRankLogVo;
import com.bitstrat.domain.CoinsRankLog;
import com.bitstrat.mapper.CoinsRankLogMapper;
import com.bitstrat.service.ICoinsRankLogService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 山寨币排行日志Service业务层处理
 *
 * @author Lion Li
 * @date 2025-04-05
 */
@RequiredArgsConstructor
@Service
public class CoinsRankLogServiceImpl implements ICoinsRankLogService {

    private final CoinsRankLogMapper baseMapper;

    /**
     * 查询山寨币排行日志
     *
     * @param id 主键
     * @return 山寨币排行日志
     */
    @Override
    public CoinsRankLogVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询山寨币排行日志列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 山寨币排行日志分页列表
     */
    @Override
    public TableDataInfo<CoinsRankLogVo> queryPageList(CoinsRankLogBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsRankLog> lqw = buildQueryWrapper(bo);
        Page<CoinsRankLogVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的山寨币排行日志列表
     *
     * @param bo 查询条件
     * @return 山寨币排行日志列表
     */
    @Override
    public List<CoinsRankLogVo> queryList(CoinsRankLogBo bo) {
        LambdaQueryWrapper<CoinsRankLog> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsRankLog> buildQueryWrapper(CoinsRankLogBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsRankLog> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsRankLog::getId);
        lqw.eq(bo.getRankId() != null, CoinsRankLog::getRankId, bo.getRankId());
        lqw.eq(bo.getScore() != null, CoinsRankLog::getScore, bo.getScore());
        lqw.eq(StringUtils.isNotBlank(bo.getMarketPrice()), CoinsRankLog::getMarketPrice, bo.getMarketPrice());
        lqw.eq(bo.getPercentage() != null, CoinsRankLog::getPercentage, bo.getPercentage());
        return lqw;
    }

    /**
     * 新增山寨币排行日志
     *
     * @param bo 山寨币排行日志
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsRankLogBo bo) {
        CoinsRankLog add = MapstructUtils.convert(bo, CoinsRankLog.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改山寨币排行日志
     *
     * @param bo 山寨币排行日志
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsRankLogBo bo) {
        CoinsRankLog update = MapstructUtils.convert(bo, CoinsRankLog.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsRankLog entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除山寨币排行日志信息
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
