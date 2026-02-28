package com.bitstrat.service.impl;

import com.bitstrat.constant.FeedStatus;
import com.bitstrat.domain.vo.CoinsFeedLikeLogVo;
import com.bitstrat.service.ICoinsFeedLikeLogService;
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
import com.bitstrat.domain.bo.CoinsFeedBo;
import com.bitstrat.domain.vo.CoinsFeedVo;
import com.bitstrat.domain.CoinsFeed;
import com.bitstrat.mapper.CoinsFeedMapper;
import com.bitstrat.service.ICoinsFeedService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 策略广场Service业务层处理
 *
 * @author Lion Li
 * @date 2025-12-12
 */
@RequiredArgsConstructor
@Service
public class CoinsFeedServiceImpl implements ICoinsFeedService {

    private final CoinsFeedMapper baseMapper;

    private final ICoinsFeedLikeLogService coinsFeedLikeLogService;
    /**
     * 查询策略广场
     *
     * @param id 主键
     * @return 策略广场
     */
    @Override
    public CoinsFeedVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询策略广场列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 策略广场分页列表
     */
    @Override
    public TableDataInfo<CoinsFeedVo> queryPageList(CoinsFeedBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsFeed> lqw = buildQueryWrapper(bo);
        Page<CoinsFeedVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        List<CoinsFeedVo> records = result.getRecords();
        List<Long> feedIds = records.stream().map(CoinsFeedVo::getId).collect(Collectors.toList());
        Long userId = LoginHelper.getUserId();
        List<CoinsFeedLikeLogVo> myLikes = coinsFeedLikeLogService.selectLogByFeedIdsAndUserId(feedIds, userId);
        Map<Long, Long> liked = myLikes.stream().collect(Collectors.toMap(CoinsFeedLikeLogVo::getFeedId, CoinsFeedLikeLogVo::getId));
        for (CoinsFeedVo record : records) {
            if (liked.containsKey(record.getId())) {
                record.setIsLiked(true);
            }else{
                record.setIsLiked(false);
            }
        }
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的策略广场列表
     *
     * @param bo 查询条件
     * @return 策略广场列表
     */
    @Override
    public List<CoinsFeedVo> queryList(CoinsFeedBo bo) {
        LambdaQueryWrapper<CoinsFeed> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsFeed> buildQueryWrapper(CoinsFeedBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsFeed> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsFeed::getId);
        lqw.eq(bo.getStrategyId() != null, CoinsFeed::getStrategyId, bo.getStrategyId());
        lqw.eq(StringUtils.isNotBlank(bo.getTitle()), CoinsFeed::getTitle, bo.getTitle());
        lqw.eq(StringUtils.isNotBlank(bo.getTags()), CoinsFeed::getTags, bo.getTags());
        lqw.eq(bo.getProfit3m() != null, CoinsFeed::getProfit3m, bo.getProfit3m());
        lqw.eq(bo.getLikeCount() != null, CoinsFeed::getLikeCount, bo.getLikeCount());
        lqw.eq(bo.getViewCount() != null, CoinsFeed::getViewCount, bo.getViewCount());
        lqw.eq(bo.getStatus() != null, CoinsFeed::getStatus, bo.getStatus());
        lqw.eq(bo.getSort() != null, CoinsFeed::getSort, bo.getSort());
        lqw.eq(bo.getShareTime() != null, CoinsFeed::getShareTime, bo.getShareTime());
        lqw.eq(bo.getUserId() != null, CoinsFeed::getUserId, bo.getUserId());
        return lqw;
    }

    /**
     * 新增策略广场
     *
     * @param bo 策略广场
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsFeedBo bo) {
        CoinsFeed add = MapstructUtils.convert(bo, CoinsFeed.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改策略广场
     *
     * @param bo 策略广场
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsFeedBo bo) {
        CoinsFeed update = MapstructUtils.convert(bo, CoinsFeed.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsFeed entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除策略广场信息
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
    public List<CoinsFeedVo> queryListByTaskIds(List<Long> taskIds) {
        if(CollectionUtils.isEmpty(taskIds)){
            return new ArrayList<>();
        }
        LambdaQueryWrapper<CoinsFeed> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CoinsFeed::getStrategyId, taskIds)
            .eq(CoinsFeed::getStatus, FeedStatus.PUBLISH.getStatus());

        return baseMapper.selectVoList(queryWrapper);
    }

    @Override
    public void updateLikeCountById(Long id, int count) {
        baseMapper.updateLikeCountById(id, count);
    }
}
