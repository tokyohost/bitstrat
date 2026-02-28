package com.bitstrat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bitstrat.constant.ArbitrageType;
import com.bitstrat.domain.CoinsArbitrageWarningConfig;
import com.bitstrat.domain.bo.CoinsArbitrageWarningConfigBo;
import com.bitstrat.domain.vo.CoinsArbitrageWarningConfigVo;
import com.bitstrat.mapper.CoinsArbitrageWarningConfigMapper;
import com.bitstrat.service.ICoinsArbitrageWarningConfigService;
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

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Objects;

/**
 * 用户配置套利警告Service业务层处理
 *
 * @author Lion Li
 * @date 2025-05-04
 */
@RequiredArgsConstructor
@Service
public class CoinsArbitrageWarningConfigServiceImpl implements ICoinsArbitrageWarningConfigService {

    private final CoinsArbitrageWarningConfigMapper baseMapper;

    /**
     * 查询用户配置套利警告
     *
     * @param id 主键
     * @return 用户配置套利警告
     */
    @Override
    public CoinsArbitrageWarningConfigVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询用户配置套利警告列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户配置套利警告分页列表
     */
    @Override
    public TableDataInfo<CoinsArbitrageWarningConfigVo> queryPageList(CoinsArbitrageWarningConfigBo bo, PageQuery pageQuery) {
        bo.setUserId(LoginHelper.getUserId());
        IPage<CoinsArbitrageWarningConfigVo> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<CoinsArbitrageWarningConfigVo> result = baseMapper.selectCoinsArbitrageWarningConfigList(bo,
            baseMapper.selectCoinsArbitrageWarningConfigList(bo, page));
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的用户配置套利警告列表
     *
     * @param bo 查询条件
     * @return 用户配置套利警告列表
     */
    @Override
    public List<CoinsArbitrageWarningConfigVo> queryList(CoinsArbitrageWarningConfigBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        LambdaQueryWrapper<CoinsArbitrageWarningConfig> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsArbitrageWarningConfig> buildQueryWrapper(CoinsArbitrageWarningConfigBo bo) {
        Map<String, Object> params = bo.getParams();
        bo.setArbitrageType(ArbitrageType.CROSS_EXCHANGE);
        LambdaQueryWrapper<CoinsArbitrageWarningConfig> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsArbitrageWarningConfig::getId);
        lqw.eq(bo.getUserId() != null, CoinsArbitrageWarningConfig::getUserId, bo.getUserId());
        lqw.eq(bo.getTaskId() != null, CoinsArbitrageWarningConfig::getTaskId, bo.getTaskId());
        lqw.eq(bo.getArbitrageType() != null, CoinsArbitrageWarningConfig::getArbitrageType, bo.getArbitrageType());
        lqw.eq(bo.getWarningThreshold() != null, CoinsArbitrageWarningConfig::getWarningThreshold, bo.getWarningThreshold());
        lqw.like(StringUtils.isNotBlank(bo.getConfigName()), CoinsArbitrageWarningConfig::getConfigName, bo.getConfigName());
        lqw.eq(bo.getUserId() != null, CoinsArbitrageWarningConfig::getUserId, bo.getUserId());
        return lqw;
    }

    /**
     * 新增用户配置套利警告
     *
     * @param bo 用户配置套利警告
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsArbitrageWarningConfigBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        CoinsArbitrageWarningConfig add = MapstructUtils.convert(bo, CoinsArbitrageWarningConfig.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改用户配置套利警告
     *
     * @param bo 用户配置套利警告
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsArbitrageWarningConfigBo bo) {
        CoinsArbitrageWarningConfig update = MapstructUtils.convert(bo, CoinsArbitrageWarningConfig.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsArbitrageWarningConfig entity){
        LambdaQueryWrapper<CoinsArbitrageWarningConfig> lqw = Wrappers.lambdaQuery();
        lqw.eq(CoinsArbitrageWarningConfig::getTaskId, entity.getTaskId())
            .eq(CoinsArbitrageWarningConfig::getUserId, entity.getUserId());
        // 如果是更新操作，排除当前记录
        if (entity.getId() != null) {
            lqw.ne(CoinsArbitrageWarningConfig::getId, entity.getId());
        }
        Long l = baseMapper.selectCount(lqw);
        if (l > 0) {
            throw new RuntimeException("您已配置了任务 "+ entity.getTaskId() + " 的套利警告，不允许重复配置");
        }
    }

    /**
     * 校验并批量删除用户配置套利警告信息
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
    public CoinsArbitrageWarningConfigVo getByTaskId(Integer arbitrageType, Long taskId) {
        CoinsArbitrageWarningConfigVo vo = baseMapper.selectVoOne(Wrappers.lambdaQuery(CoinsArbitrageWarningConfig.class)
            .eq(CoinsArbitrageWarningConfig::getTaskId, taskId)
            .eq(CoinsArbitrageWarningConfig::getArbitrageType, arbitrageType)
            .eq(CoinsArbitrageWarningConfig::getUserId, LoginHelper.getUserId()));
        if (Objects.isNull(vo)){
            return new CoinsArbitrageWarningConfigVo();
        }
        return vo;
    }
}
