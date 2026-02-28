package com.bitstrat.service.impl;

import com.bitstrat.domain.CoinsNotifyLog;
import com.bitstrat.domain.bo.CoinsNotifyLogBo;
import com.bitstrat.domain.vo.CoinsNotifyLogVo;
import com.bitstrat.mapper.CoinsNotifyLogMapper;
import com.bitstrat.service.ICoinsNotifyLogService;
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
 * 通知日志Service业务层处理
 *
 * @author Lion Li
 * @date 2025-04-26
 */
@RequiredArgsConstructor
@Service
public class CoinsNotifyLogServiceImpl implements ICoinsNotifyLogService {

    private final CoinsNotifyLogMapper baseMapper;

    /**
     * 查询通知日志
     *
     * @param id 主键
     * @return 通知日志
     */
    @Override
    public CoinsNotifyLogVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询通知日志列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 通知日志分页列表
     */
    @Override
    public TableDataInfo<CoinsNotifyLogVo> queryPageList(CoinsNotifyLogBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsNotifyLog> lqw = buildQueryWrapper(bo);
        Page<CoinsNotifyLogVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的通知日志列表
     *
     * @param bo 查询条件
     * @return 通知日志列表
     */
    @Override
    public List<CoinsNotifyLogVo> queryList(CoinsNotifyLogBo bo) {
        LambdaQueryWrapper<CoinsNotifyLog> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsNotifyLog> buildQueryWrapper(CoinsNotifyLogBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsNotifyLog> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsNotifyLog::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getNotifyType()), CoinsNotifyLog::getNotifyType, bo.getNotifyType());
        lqw.eq(StringUtils.isNotBlank(bo.getNotifyContent()), CoinsNotifyLog::getNotifyContent, bo.getNotifyContent());
        lqw.eq(StringUtils.isNotBlank(bo.getNotifyStatus()), CoinsNotifyLog::getNotifyStatus, bo.getNotifyStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getErrorMessage()), CoinsNotifyLog::getErrorMessage, bo.getErrorMessage());
        lqw.eq(bo.getUserId() != null, CoinsNotifyLog::getUserId, bo.getUserId());
        return lqw;
    }

    /**
     * 新增通知日志
     *
     * @param bo 通知日志
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsNotifyLogBo bo) {
        CoinsNotifyLog add = MapstructUtils.convert(bo, CoinsNotifyLog.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改通知日志
     *
     * @param bo 通知日志
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsNotifyLogBo bo) {
        CoinsNotifyLog update = MapstructUtils.convert(bo, CoinsNotifyLog.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsNotifyLog entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除通知日志信息
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
