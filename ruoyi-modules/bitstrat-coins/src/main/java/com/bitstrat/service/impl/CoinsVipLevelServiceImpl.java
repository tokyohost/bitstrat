package com.bitstrat.service.impl;

import com.bitstrat.domain.CoinsVipLevel;
import com.bitstrat.domain.bo.CoinsVipLevelBo;
import com.bitstrat.domain.vo.CoinsVipLevelVo;
import com.bitstrat.mapper.CoinsVipLevelMapper;
import com.bitstrat.service.ICoinsVipLevelService;
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
 * VIP 权限Service业务层处理
 *
 * @author Lion Li
 * @date 2025-05-14
 */
@RequiredArgsConstructor
@Service
public class CoinsVipLevelServiceImpl implements ICoinsVipLevelService {

    private final CoinsVipLevelMapper baseMapper;

    /**
     * 查询VIP 权限
     *
     * @param id 主键
     * @return VIP 权限
     */
    @Override
    public CoinsVipLevelVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询VIP 权限列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return VIP 权限分页列表
     */
    @Override
    public TableDataInfo<CoinsVipLevelVo> queryPageList(CoinsVipLevelBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsVipLevel> lqw = buildQueryWrapper(bo);
        Page<CoinsVipLevelVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的VIP 权限列表
     *
     * @param bo 查询条件
     * @return VIP 权限列表
     */
    @Override
    public List<CoinsVipLevelVo> queryList(CoinsVipLevelBo bo) {
        LambdaQueryWrapper<CoinsVipLevel> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsVipLevel> buildQueryWrapper(CoinsVipLevelBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsVipLevel> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsVipLevel::getId);
        lqw.like(StringUtils.isNotBlank(bo.getName()), CoinsVipLevel::getName, bo.getName());
        lqw.eq(bo.getLevel() != null, CoinsVipLevel::getLevel, bo.getLevel());
        lqw.eq(bo.getMaxAbAmount() != null, CoinsVipLevel::getMaxAbAmount, bo.getMaxAbAmount());
        lqw.eq(bo.getMaxActiveTask() != null, CoinsVipLevel::getMaxActiveTask, bo.getMaxActiveTask());
        lqw.eq(bo.getStatus() != null, CoinsVipLevel::getStatus, bo.getStatus());
        lqw.eq(bo.getPrice() != null, CoinsVipLevel::getPrice, bo.getPrice());
        lqw.eq(bo.getAvaliableDay() != null, CoinsVipLevel::getAvaliableDay, bo.getAvaliableDay());
        return lqw;
    }

    /**
     * 新增VIP 权限
     *
     * @param bo VIP 权限
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsVipLevelBo bo) {
        CoinsVipLevel add = MapstructUtils.convert(bo, CoinsVipLevel.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改VIP 权限
     *
     * @param bo VIP 权限
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsVipLevelBo bo) {
        CoinsVipLevel update = MapstructUtils.convert(bo, CoinsVipLevel.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsVipLevel entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除VIP 权限信息
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
    public List<CoinsVipLevelVo> getAvailableVipLevelList(CoinsVipLevelBo bo) {
        LambdaQueryWrapper<CoinsVipLevel> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsVipLevel::getLevel);
        lqw.eq(CoinsVipLevel::getStatus, 1);
        return baseMapper.selectVoList(lqw);
    }
}
