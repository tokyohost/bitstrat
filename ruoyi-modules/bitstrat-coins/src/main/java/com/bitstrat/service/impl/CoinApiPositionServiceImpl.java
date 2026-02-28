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
import com.bitstrat.domain.bo.CoinApiPositionBo;
import com.bitstrat.domain.vo.CoinApiPositionVo;
import com.bitstrat.domain.CoinApiPosition;
import com.bitstrat.mapper.CoinApiPositionMapper;
import com.bitstrat.service.ICoinApiPositionService;

import java.math.BigDecimal;
import java.util.*;

/**
 * API 历史仓位数据Service业务层处理
 *
 * @author Lion Li
 * @date 2025-12-29
 */
@RequiredArgsConstructor
@Service
public class CoinApiPositionServiceImpl implements ICoinApiPositionService {

    private final CoinApiPositionMapper baseMapper;

    /**
     * 查询API 历史仓位数据
     *
     * @param id 主键
     * @return API 历史仓位数据
     */
    @Override
    public CoinApiPositionVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询API 历史仓位数据列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return API 历史仓位数据分页列表
     */
    @Override
    public TableDataInfo<CoinApiPositionVo> queryPageList(CoinApiPositionBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinApiPosition> lqw = buildQueryWrapper(bo);
        Page<CoinApiPositionVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的API 历史仓位数据列表
     *
     * @param bo 查询条件
     * @return API 历史仓位数据列表
     */
    @Override
    public List<CoinApiPositionVo> queryList(CoinApiPositionBo bo) {
        LambdaQueryWrapper<CoinApiPosition> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinApiPosition> buildQueryWrapper(CoinApiPositionBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinApiPosition> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinApiPosition::getId);
        lqw.eq(bo.getApiId() != null, CoinApiPosition::getApiId, bo.getApiId());
        lqw.eq(bo.getUserId() != null, CoinApiPosition::getUserId, bo.getUserId());
        lqw.eq(StringUtils.isNotBlank(bo.getPositionId()), CoinApiPosition::getPositionId, bo.getPositionId());
        lqw.eq(StringUtils.isNotBlank(bo.getCoin()), CoinApiPosition::getCoin, bo.getCoin());
        lqw.eq(StringUtils.isNotBlank(bo.getSide()), CoinApiPosition::getSide, bo.getSide());
        lqw.eq(bo.getSize() != null, CoinApiPosition::getSize, bo.getSize());
        lqw.eq(bo.getOpen() != null, CoinApiPosition::getOpen, bo.getOpen());
        lqw.eq(bo.getClose() != null, CoinApiPosition::getClose, bo.getClose());
        lqw.eq(StringUtils.isNotBlank(bo.getMarginMode()), CoinApiPosition::getMarginMode, bo.getMarginMode());
        lqw.eq(bo.getNetProfit() != null, CoinApiPosition::getNetProfit, bo.getNetProfit());
        lqw.eq(bo.getPnl() != null, CoinApiPosition::getPnl, bo.getPnl());
        lqw.eq(bo.getTotalFunding() != null, CoinApiPosition::getTotalFunding, bo.getTotalFunding());
        lqw.eq(bo.getOpenFee() != null, CoinApiPosition::getOpenFee, bo.getOpenFee());
        lqw.eq(bo.getCloseFee() != null, CoinApiPosition::getCloseFee, bo.getCloseFee());
        lqw.eq(StringUtils.isNotBlank(bo.getUtime()), CoinApiPosition::getUtime, bo.getUtime());
        lqw.eq(StringUtils.isNotBlank(bo.getCtime()), CoinApiPosition::getCtime, bo.getCtime());
        lqw.eq(bo.getTime() != null, CoinApiPosition::getTime, bo.getTime());
        return lqw;
    }

    /**
     * 新增API 历史仓位数据
     *
     * @param bo API 历史仓位数据
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinApiPositionBo bo) {
        CoinApiPosition add = MapstructUtils.convert(bo, CoinApiPosition.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改API 历史仓位数据
     *
     * @param bo API 历史仓位数据
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinApiPositionBo bo) {
        CoinApiPosition update = MapstructUtils.convert(bo, CoinApiPosition.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinApiPosition entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除API 历史仓位数据信息
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
    public List<String> selectIdsByApiIdAndCurrentId(List<String> posIds, Long apiId) {
        if(CollectionUtils.isNotEmpty(posIds)){
            return baseMapper.selectIdsByApiIdAndCurrentId(posIds, apiId);
        }

        return List.of();
    }

    @Override
    public Double querySharpeRatioByApiIdAndStartTime(Long apiId, Date createTime) {
        long time = createTime.getTime();
        BigDecimal bigDecimal = baseMapper.querySharpeRatioByApiIdAndStartTime(apiId, time);
        if(Objects.nonNull(bigDecimal)){
            return bigDecimal.setScale(4,BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        return 0.0;
    }
}
