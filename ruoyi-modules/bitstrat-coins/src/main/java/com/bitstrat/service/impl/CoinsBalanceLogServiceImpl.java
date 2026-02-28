package com.bitstrat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bitstrat.constant.CoinsBalanceStatus;
import com.bitstrat.domain.CoinsAiTask;
import com.bitstrat.domain.CoinsBalanceLog;
import com.bitstrat.domain.bo.CoinAiTaskRequestBo;
import com.bitstrat.domain.bo.CoinsAiTaskBo;
import com.bitstrat.domain.bo.CoinsBalanceLogBo;
import com.bitstrat.domain.vo.CoinsBalanceLogVo;
import com.bitstrat.mapper.CoinsBalanceLogMapper;
import com.bitstrat.service.ICoinsAiTaskService;
import com.bitstrat.service.ICoinsBalanceLogService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.SysUser;
import org.dromara.system.domain.vo.SysUserVo;
import org.dromara.system.mapper.SysUserMapper;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 账户余额变动日志Service业务层处理
 *
 * @author Lion Li
 * @date 2025-11-20
 */
@RequiredArgsConstructor
@Service
public class CoinsBalanceLogServiceImpl implements ICoinsBalanceLogService {

    private final CoinsBalanceLogMapper baseMapper;
    private final SysUserMapper sysUserMapper;
    private final ICoinsAiTaskService aiTaskService;
    private final RedissonClient redissonClient;

    /**
     * 查询账户余额变动日志
     *
     * @param id 主键
     * @return 账户余额变动日志
     */
    @Override
    public CoinsBalanceLogVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询账户余额变动日志列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 账户余额变动日志分页列表
     */
    @Override
    public TableDataInfo<CoinsBalanceLogVo> queryPageList(CoinsBalanceLogBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsBalanceLog> lqw = buildQueryWrapper(bo);
        Page<CoinsBalanceLogVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的账户余额变动日志列表
     *
     * @param bo 查询条件
     * @return 账户余额变动日志列表
     */
    @Override
    public List<CoinsBalanceLogVo> queryList(CoinsBalanceLogBo bo) {
        LambdaQueryWrapper<CoinsBalanceLog> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsBalanceLog> buildQueryWrapper(CoinsBalanceLogBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsBalanceLog> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsBalanceLog::getId);
        lqw.eq(bo.getUserId() != null, CoinsBalanceLog::getUserId, bo.getUserId());
        lqw.eq(bo.getBeforeBalance() != null, CoinsBalanceLog::getBeforeBalance, bo.getBeforeBalance());
        lqw.eq(bo.getChangeAmount() != null, CoinsBalanceLog::getChangeAmount, bo.getChangeAmount());
        lqw.eq(bo.getAfterBalance() != null, CoinsBalanceLog::getAfterBalance, bo.getAfterBalance());
        lqw.eq(bo.getType() != null, CoinsBalanceLog::getType, bo.getType());
        return lqw;
    }

    /**
     * 新增账户余额变动日志
     *
     * @param bo 账户余额变动日志
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsBalanceLogBo bo) {
        CoinsBalanceLog add = MapstructUtils.convert(bo, CoinsBalanceLog.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改账户余额变动日志
     *
     * @param bo 账户余额变动日志
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsBalanceLogBo bo) {
        CoinsBalanceLog update = MapstructUtils.convert(bo, CoinsBalanceLog.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsBalanceLog entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除账户余额变动日志信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reduceBalanceByLog(CoinsBalanceLog coinsBalanceLog) {
        baseMapper.reduceBalanceByUserId(coinsBalanceLog.getChangeAmount().abs(), coinsBalanceLog.getUserId());
        baseMapper.insert(coinsBalanceLog);
    }

    @Override
    public void addBalanceByUserId(BigDecimal amount, Long userId) {
        baseMapper.addBalanceByUserId(amount, userId);
    }

    @Override
    public CoinsBalanceLogBo queryBoById(String outTradeNo) {


        return null;
    }

    @Override

    @Transactional(rollbackFor = Exception.class)
    public synchronized void updateOrderPaid(String outTradeNo, String tradeNo, String tradeStatus) {
        CoinsBalanceLog coinsBalanceLog = baseMapper.selectById(outTradeNo);
        if (Objects.nonNull(coinsBalanceLog)) {
            coinsBalanceLog.setTradeNo(tradeNo);
            coinsBalanceLog.setTradeStatus(tradeStatus);
            //累加余额
            if (coinsBalanceLog.getStatus() == 1L) {
                coinsBalanceLog.setStatus(2L);
                baseMapper.addBalanceByUserId(coinsBalanceLog.getChangeAmount(), coinsBalanceLog.getUserId());
                SysUser sysUser = sysUserMapper.selectById(coinsBalanceLog.getUserId());
                coinsBalanceLog.setAfterBalance(sysUser.getBalance());
                baseMapper.updateById(coinsBalanceLog);
            }


        } else {
            throw new RuntimeException("未找到支付订单");
        }

    }

    @Override
    public void checkBalance(CoinAiTaskRequestBo coinTestAiRequest, CoinsAiTask coinsAiTask) {
        SysUserVo sysUserVo = sysUserMapper.selectVoById(coinsAiTask.getCreateUserId());
        if (sysUserVo.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            aiTaskService.stopTask(coinsAiTask);
            throw new RuntimeException("余额不足，请先充值");
        }
    }

    @Override
    public void checkBalance(CoinsAiTaskBo coinsAiTask) {
        SysUserVo sysUserVo = sysUserMapper.selectVoById(coinsAiTask.getCreateUserId());
        if (sysUserVo.getBalance().compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException("余额不足，请先充值");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderFail(String outTradeNo, String tradeNo, String tradeStatus) {
        CoinsBalanceLog coinsBalanceLog = baseMapper.selectById(outTradeNo);
        if (Objects.nonNull(coinsBalanceLog)) {
            coinsBalanceLog.setTradeNo(tradeNo);
            coinsBalanceLog.setTradeStatus(tradeStatus);
            coinsBalanceLog.setStatus(CoinsBalanceStatus.ERROR.getStatus());
            baseMapper.updateById(coinsBalanceLog);
        } else {
            throw new RuntimeException("未找到支付订单");
        }

    }

    @Override
    public void updateOrderCancel(String outTradeNo, String tradeNo, String tradeStatus) {
        CoinsBalanceLog coinsBalanceLog = baseMapper.selectById(outTradeNo);
        if (Objects.nonNull(coinsBalanceLog)) {
            coinsBalanceLog.setTradeNo(tradeNo);
            coinsBalanceLog.setTradeStatus(tradeStatus);
            coinsBalanceLog.setStatus(CoinsBalanceStatus.CANCEL.getStatus());
            baseMapper.updateById(coinsBalanceLog);
        } else {
            throw new RuntimeException("未找到支付订单");
        }
    }

}
