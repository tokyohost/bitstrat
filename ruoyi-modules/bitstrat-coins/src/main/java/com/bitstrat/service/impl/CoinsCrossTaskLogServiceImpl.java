package com.bitstrat.service.impl;

import com.bitstrat.utils.ThreadLocalLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.bitstrat.domain.bo.CoinsCrossTaskLogBo;
import com.bitstrat.domain.vo.CoinsCrossTaskLogVo;
import com.bitstrat.domain.CoinsCrossTaskLog;
import com.bitstrat.mapper.CoinsCrossTaskLogMapper;
import com.bitstrat.service.ICoinsCrossTaskLogService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 跨交易所任务日志Service业务层处理
 *
 * @author Lion Li
 * @date 2025-04-19
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class CoinsCrossTaskLogServiceImpl implements ICoinsCrossTaskLogService {
    ExecutorService logSavePool = Executors.newWorkStealingPool(2);
    private final CoinsCrossTaskLogMapper baseMapper;

    /**
     * 查询跨交易所任务日志
     *
     * @param id 主键
     * @return 跨交易所任务日志
     */
    @Override
    public CoinsCrossTaskLogVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询跨交易所任务日志列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 跨交易所任务日志分页列表
     */
    @Override
    public TableDataInfo<CoinsCrossTaskLogVo> queryPageList(CoinsCrossTaskLogBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsCrossTaskLog> lqw = buildQueryWrapper(bo);
        Page<CoinsCrossTaskLogVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的跨交易所任务日志列表
     *
     * @param bo 查询条件
     * @return 跨交易所任务日志列表
     */
    @Override
    public List<CoinsCrossTaskLogVo> queryList(CoinsCrossTaskLogBo bo) {
        LambdaQueryWrapper<CoinsCrossTaskLog> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsCrossTaskLog> buildQueryWrapper(CoinsCrossTaskLogBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsCrossTaskLog> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsCrossTaskLog::getId);
        lqw.eq(bo.getTaskId() != null, CoinsCrossTaskLog::getTaskId, bo.getTaskId());
        lqw.eq(StringUtils.isNotBlank(bo.getMsg()), CoinsCrossTaskLog::getMsg, bo.getMsg());
        return lqw;
    }

    /**
     * 新增跨交易所任务日志
     *
     * @param bo 跨交易所任务日志
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsCrossTaskLogBo bo) {
        CoinsCrossTaskLog add = MapstructUtils.convert(bo, CoinsCrossTaskLog.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改跨交易所任务日志
     *
     * @param bo 跨交易所任务日志
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsCrossTaskLogBo bo) {
        CoinsCrossTaskLog update = MapstructUtils.convert(bo, CoinsCrossTaskLog.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsCrossTaskLog entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除跨交易所任务日志信息
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
    public void saveLog(Long id, String s) {
        ThreadLocalLogUtil.log(s);
        log.info(s);
        CoinsCrossTaskLog coinsCrossTaskLogBo = new CoinsCrossTaskLog();
        coinsCrossTaskLogBo.setTaskId(id);
        coinsCrossTaskLogBo.setMsg(s);
        coinsCrossTaskLogBo.setCreateTime(new Date());
        logSavePool.execute(() -> {
            baseMapper.insert(coinsCrossTaskLogBo);
        });

    }
}
