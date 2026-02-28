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
import com.bitstrat.domain.bo.CoinsAiConfigBo;
import com.bitstrat.domain.vo.CoinsAiConfigVo;
import com.bitstrat.domain.CoinsAiConfig;
import com.bitstrat.mapper.CoinsAiConfigMapper;
import com.bitstrat.service.ICoinsAiConfigService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * ai 流水线配置Service业务层处理
 *
 * @author Lion Li
 * @date 2025-04-01
 */
@RequiredArgsConstructor
@Service
public class CoinsAiConfigServiceImpl implements ICoinsAiConfigService {

    private final CoinsAiConfigMapper baseMapper;

    /**
     * 查询ai 流水线配置
     *
     * @param id 主键
     * @return ai 流水线配置
     */
    @Override
    public CoinsAiConfigVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询ai 流水线配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return ai 流水线配置分页列表
     */
    @Override
    public TableDataInfo<CoinsAiConfigVo> queryPageList(CoinsAiConfigBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsAiConfig> lqw = buildQueryWrapper(bo);
        Page<CoinsAiConfigVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的ai 流水线配置列表
     *
     * @param bo 查询条件
     * @return ai 流水线配置列表
     */
    @Override
    public List<CoinsAiConfigVo> queryList(CoinsAiConfigBo bo) {
        LambdaQueryWrapper<CoinsAiConfig> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsAiConfig> buildQueryWrapper(CoinsAiConfigBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsAiConfig> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsAiConfig::getId);
        lqw.like(StringUtils.isNotBlank(bo.getFlowName()), CoinsAiConfig::getFlowName, bo.getFlowName());
        lqw.eq(StringUtils.isNotBlank(bo.getUrl()), CoinsAiConfig::getUrl, bo.getUrl());
        lqw.eq(StringUtils.isNotBlank(bo.getToken()), CoinsAiConfig::getToken, bo.getToken());
        lqw.eq(StringUtils.isNotBlank(bo.getType()), CoinsAiConfig::getType, bo.getType());
        return lqw;
    }

    /**
     * 新增ai 流水线配置
     *
     * @param bo ai 流水线配置
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsAiConfigBo bo) {
        CoinsAiConfig add = MapstructUtils.convert(bo, CoinsAiConfig.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改ai 流水线配置
     *
     * @param bo ai 流水线配置
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsAiConfigBo bo) {
        CoinsAiConfig update = MapstructUtils.convert(bo, CoinsAiConfig.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsAiConfig entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除ai 流水线配置信息
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
    public List<CoinsAiConfigVo> querySelectList(CoinsAiConfigBo bo) {
        LambdaQueryWrapper<CoinsAiConfig> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }
}
