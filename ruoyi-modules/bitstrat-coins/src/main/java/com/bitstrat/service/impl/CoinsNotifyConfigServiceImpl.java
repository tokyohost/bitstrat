package com.bitstrat.service.impl;

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
import com.bitstrat.domain.bo.CoinsNotifyConfigBo;
import com.bitstrat.domain.vo.CoinsNotifyConfigVo;
import com.bitstrat.domain.CoinsNotifyConfig;
import com.bitstrat.mapper.CoinsNotifyConfigMapper;
import com.bitstrat.service.ICoinsNotifyConfigService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 用户通知设置Service业务层处理
 *
 * @author Lion Li
 * @date 2025-04-25
 */
@RequiredArgsConstructor
@Service
public class CoinsNotifyConfigServiceImpl implements ICoinsNotifyConfigService {

    private final CoinsNotifyConfigMapper baseMapper;

    /**
     * 查询用户通知设置
     *
     * @param id 主键
     * @return 用户通知设置
     */
    @Override
    public CoinsNotifyConfigVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询用户通知设置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户通知设置分页列表
     */
    @Override
    public TableDataInfo<CoinsNotifyConfigVo> queryPageList(CoinsNotifyConfigBo bo, PageQuery pageQuery) {
        bo.setUserId(LoginHelper.getUserId());
        LambdaQueryWrapper<CoinsNotifyConfig> lqw = buildQueryWrapper(bo);
        Page<CoinsNotifyConfigVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的用户通知设置列表
     *
     * @param bo 查询条件
     * @return 用户通知设置列表
     */
    @Override
    public List<CoinsNotifyConfigVo> queryList(CoinsNotifyConfigBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        LambdaQueryWrapper<CoinsNotifyConfig> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsNotifyConfig> buildQueryWrapper(CoinsNotifyConfigBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsNotifyConfig> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getType()), CoinsNotifyConfig::getType, bo.getType());
        lqw.eq(StringUtils.isNotBlank(bo.getDingToken()), CoinsNotifyConfig::getDingToken, bo.getDingToken());
        lqw.eq(StringUtils.isNotBlank(bo.getDingSecret()), CoinsNotifyConfig::getDingSecret, bo.getDingSecret());
        lqw.eq(StringUtils.isNotBlank(bo.getTelegramChatId()), CoinsNotifyConfig::getTelegramChatId, bo.getTelegramChatId());
        lqw.eq(bo.getUserId() != null, CoinsNotifyConfig::getUserId, bo.getUserId());
        return lqw;
    }

    /**
     * 新增用户通知设置
     *
     * @param bo 用户通知设置
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsNotifyConfigBo bo) {
        CoinsNotifyConfig add = MapstructUtils.convert(bo, CoinsNotifyConfig.class);
        Long userId = LoginHelper.getUserId();
        assert add != null;
        add.setUserId(userId);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改用户通知设置
     *
     * @param bo 用户通知设置
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsNotifyConfigBo bo) {
        CoinsNotifyConfig update = MapstructUtils.convert(bo, CoinsNotifyConfig.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsNotifyConfig entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除用户通知设置信息
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
    public List<CoinsNotifyConfigVo> queryConfigByUserId(Long userId) {
        LambdaQueryWrapper<CoinsNotifyConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CoinsNotifyConfig::getUserId, userId);

        return baseMapper.selectVoList(queryWrapper);
    }
}
