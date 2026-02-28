package com.bitstrat.service.impl;

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
import com.bitstrat.domain.bo.CoinsFeedLikeLogBo;
import com.bitstrat.domain.vo.CoinsFeedLikeLogVo;
import com.bitstrat.domain.CoinsFeedLikeLog;
import com.bitstrat.mapper.CoinsFeedLikeLogMapper;
import com.bitstrat.service.ICoinsFeedLikeLogService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 策略广场like日志Service业务层处理
 *
 * @author Lion Li
 * @date 2025-12-12
 */
@RequiredArgsConstructor
@Service
public class CoinsFeedLikeLogServiceImpl implements ICoinsFeedLikeLogService {

    private final CoinsFeedLikeLogMapper baseMapper;

    /**
     * 查询策略广场like日志
     *
     * @param id 主键
     * @return 策略广场like日志
     */
    @Override
    public CoinsFeedLikeLogVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询策略广场like日志列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 策略广场like日志分页列表
     */
    @Override
    public TableDataInfo<CoinsFeedLikeLogVo> queryPageList(CoinsFeedLikeLogBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsFeedLikeLog> lqw = buildQueryWrapper(bo);
        Page<CoinsFeedLikeLogVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的策略广场like日志列表
     *
     * @param bo 查询条件
     * @return 策略广场like日志列表
     */
    @Override
    public List<CoinsFeedLikeLogVo> queryList(CoinsFeedLikeLogBo bo) {
        LambdaQueryWrapper<CoinsFeedLikeLog> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsFeedLikeLog> buildQueryWrapper(CoinsFeedLikeLogBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsFeedLikeLog> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsFeedLikeLog::getId);
        lqw.eq(bo.getFeedId() != null, CoinsFeedLikeLog::getFeedId, bo.getFeedId());
        lqw.eq(bo.getUserId() != null, CoinsFeedLikeLog::getUserId, bo.getUserId());
        return lqw;
    }

    /**
     * 新增策略广场like日志
     *
     * @param bo 策略广场like日志
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsFeedLikeLogBo bo) {
        CoinsFeedLikeLog add = MapstructUtils.convert(bo, CoinsFeedLikeLog.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改策略广场like日志
     *
     * @param bo 策略广场like日志
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsFeedLikeLogBo bo) {
        CoinsFeedLikeLog update = MapstructUtils.convert(bo, CoinsFeedLikeLog.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsFeedLikeLog entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除策略广场like日志信息
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
    public CoinsFeedLikeLogVo selectLogByFeedIdAndUserId(Long id, Long userId) {

        return baseMapper.selectVoOne(Wrappers.<CoinsFeedLikeLog>lambdaQuery().eq(CoinsFeedLikeLog::getFeedId, id).eq(CoinsFeedLikeLog::getUserId, userId));
    }

    @Override
    public List<CoinsFeedLikeLogVo> selectLogByFeedIdsAndUserId(List<Long> feedIds, Long userId) {
        if(CollectionUtils.isEmpty(feedIds)){
            return List.of();
        }

        return baseMapper.selectVoList(Wrappers.<CoinsFeedLikeLog>lambdaQuery().in(CoinsFeedLikeLog::getFeedId, feedIds).eq(CoinsFeedLikeLog::getUserId, userId));
    }
}
